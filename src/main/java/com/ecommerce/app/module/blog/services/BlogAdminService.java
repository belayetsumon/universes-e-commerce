package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.dto.BlogDashboardMetrics;
import com.ecommerce.app.module.blog.dto.BlogForm;
import com.ecommerce.app.module.blog.dto.BlogSearchCriteria;
import com.ecommerce.app.module.blog.mapper.BlogMapper;
import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogApproval;
import com.ecommerce.app.module.blog.model.BlogApprovalDecision;
import com.ecommerce.app.module.blog.model.BlogCategory;
import com.ecommerce.app.module.blog.model.BlogComment;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogRecordStatus;
import com.ecommerce.app.module.blog.model.BlogRevision;
import com.ecommerce.app.module.blog.model.BlogSeo;
import com.ecommerce.app.module.blog.model.BlogTag;
import com.ecommerce.app.module.blog.model.BlogModerationStatus;
import com.ecommerce.app.module.blog.repository.BlogApprovalRepository;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogCommentRepository;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import com.ecommerce.app.module.blog.repository.BlogRevisionRepository;
import com.ecommerce.app.module.blog.repository.BlogSeriesRepository;
import com.ecommerce.app.module.blog.repository.BlogTagRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BlogAdminService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogTagRepository tagRepository;
    private final BlogSeriesRepository seriesRepository;
    private final BlogRevisionRepository revisionRepository;
    private final BlogApprovalRepository approvalRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogMapper mapper;
    private final BlogHtmlSanitizer htmlSanitizer;
    private final BlogImageStorageService imageStorageService;

    public BlogAdminService(
            BlogRepository blogRepository,
            BlogCategoryRepository categoryRepository,
            BlogTagRepository tagRepository,
            BlogSeriesRepository seriesRepository,
            BlogRevisionRepository revisionRepository,
            BlogApprovalRepository approvalRepository,
            BlogCommentRepository commentRepository,
            BlogMapper mapper,
            BlogHtmlSanitizer htmlSanitizer,
            BlogImageStorageService imageStorageService) {
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.seriesRepository = seriesRepository;
        this.revisionRepository = revisionRepository;
        this.approvalRepository = approvalRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
        this.htmlSanitizer = htmlSanitizer;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public Page<Blog> search(BlogSearchCriteria criteria, Pageable pageable) {
        return blogRepository.findAll(specification(criteria), pageable);
    }

    @Transactional(readOnly = true)
    public Blog findRequired(Long id) {
        return blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blog post was not found."));
    }

    @Transactional
    public Blog save(BlogForm form) {
        return save(form, null);
    }

    @Transactional
    public Blog save(BlogForm form, MultipartFile featuredImageFile) {
        Blog blog = form.getId() == null ? new Blog() : findRequired(form.getId());
        assertVersion(form, blog);
        form.setSlug(uniqueBlogSlug(form.getSlug(), form.getTitle(), form.getLanguageCode(), form.getId()));
        applyFeaturedImageUpload(form, featuredImageFile);
        form.setContentHtml(htmlSanitizer.sanitize(form.getContentHtml()));
        mapper.copyFormToBlog(form, blog);
        blog.setCategory(form.getCategoryId() == null ? null : categoryRepository.findById(form.getCategoryId()).orElse(null));
        blog.setSeries(form.getSeriesId() == null ? null : seriesRepository.findById(form.getSeriesId()).orElse(null));
        blog.setTags(resolveTags(form.getTags()));
        if (blog.getStatus() == BlogPublicationStatus.PUBLISHED && blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        BlogSeo seo = blog.getSeo();
        if (seo == null) {
            seo = new BlogSeo();
            seo.setBlog(blog);
            blog.setSeo(seo);
        }
        mapper.copyFormToSeo(form, seo);
        Blog saved = blogRepository.save(blog);
        createRevision(saved, form.getChangeSummary());
        if (saved.getStatus() == BlogPublicationStatus.IN_REVIEW) {
            createApprovalRequest(saved);
        }
        return saved;
    }

    private void applyFeaturedImageUpload(BlogForm form, MultipartFile featuredImageFile) {
        if (!imageStorageService.hasFile(featuredImageFile)) {
            return;
        }
        try {
            form.setFeaturedImageUrl(imageStorageService.storeFeaturedImage(featuredImageFile));
        } catch (java.io.IOException ex) {
            throw new BlogImageUploadException("Featured image upload failed. " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public Blog duplicate(Long id) {
        Blog source = findRequired(id);
        Blog copy = new Blog();
        copy.setTitle(source.getTitle() + " Copy");
        copy.setSlug(uniqueCopySlug(source.getSlug()));
        copy.setExcerpt(source.getExcerpt());
        copy.setContentHtml(source.getContentHtml());
        copy.setContentPlainText(source.getContentPlainText());
        copy.setStatus(BlogPublicationStatus.DRAFT);
        copy.setVisibility(source.getVisibility());
        copy.setCategory(source.getCategory());
        copy.setSeries(source.getSeries());
        copy.setTags(new LinkedHashSet<>(source.getTags()));
        copy.setFeaturedImageUrl(source.getFeaturedImageUrl());
        copy.setFeaturedImageAlt(source.getFeaturedImageAlt());
        copy.setLanguageCode(source.getLanguageCode());
        copy.setAllowComments(source.isAllowComments());
        copy.setReadingTimeMinutes(source.getReadingTimeMinutes());
        Blog saved = blogRepository.save(copy);
        createRevision(saved, "Duplicated from blog #" + source.getId());
        return saved;
    }

    @Transactional
    public void softDelete(Long id) {
        Blog blog = findRequired(id);
        blog.setDeletedFlag(true);
        blog.setActiveFlag(false);
        blog.setRecordStatus(BlogRecordStatus.DELETED);
        blog.setDeletedAt(LocalDateTime.now());
        blog.setDeletedBy(currentUsername());
        blogRepository.save(blog);
    }

    @Transactional
    public void changeStatus(Long id, BlogPublicationStatus status) {
        Blog blog = findRequired(id);
        blog.setStatus(status);
        if (status == BlogPublicationStatus.PUBLISHED) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        if (status == BlogPublicationStatus.ARCHIVED) {
            blog.setRecordStatus(BlogRecordStatus.ARCHIVED);
            blog.setActiveFlag(false);
        }
        blogRepository.save(blog);
        createRevision(blog, "Status changed to " + status);
    }

    @Transactional
    public void approve(Long id, String remarks) {
        Blog blog = findRequired(id);
        blog.setStatus(BlogPublicationStatus.APPROVED);
        BlogApproval approval = new BlogApproval();
        approval.setBlog(blog);
        approval.setReviewer(currentUsername());
        approval.setDecision(BlogApprovalDecision.APPROVED);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(approval);
    }

    @Transactional
    public void reject(Long id, String remarks) {
        Blog blog = findRequired(id);
        blog.setStatus(BlogPublicationStatus.NEEDS_CHANGES);
        BlogApproval approval = new BlogApproval();
        approval.setBlog(blog);
        approval.setReviewer(currentUsername());
        approval.setDecision(BlogApprovalDecision.RETURNED_FOR_CHANGES);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(approval);
    }

    @Transactional
    public void moderateComment(Long commentId, BlogModerationStatus status) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment was not found."));
        comment.setModerationStatus(status);
        commentRepository.save(comment);
    }

    @Transactional
    public int publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        var posts = blogRepository.findByStatusAndScheduledAtLessThanEqualAndDeletedFlagFalse(BlogPublicationStatus.SCHEDULED, now);
        posts.forEach(post -> {
            post.setStatus(BlogPublicationStatus.PUBLISHED);
            post.setPublishedAt(now);
        });
        blogRepository.saveAll(posts);
        return posts.size();
    }

    @Transactional(readOnly = true)
    public BlogDashboardMetrics dashboardMetrics() {
        return new BlogDashboardMetrics(
                blogRepository.countByDeletedFlagFalse(),
                blogRepository.countByStatusAndDeletedFlagFalse(BlogPublicationStatus.PUBLISHED),
                blogRepository.countByStatusAndDeletedFlagFalse(BlogPublicationStatus.DRAFT),
                blogRepository.countByStatusAndDeletedFlagFalse(BlogPublicationStatus.IN_REVIEW),
                blogRepository.countByStatusAndDeletedFlagFalse(BlogPublicationStatus.SCHEDULED),
                commentRepository.countByModerationStatusAndDeletedFlagFalse(BlogModerationStatus.PENDING));
    }

    private Specification<Blog> specification(BlogSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.isFalse(root.get("deletedFlag"));
            if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
                String like = "%" + criteria.getQuery().trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("excerpt"), like),
                        cb.like(root.get("contentPlainText"), like)));
            }
            if (criteria.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getVisibility() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("visibility"), criteria.getVisibility()));
            }
            if (criteria.getCategoryId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }
            return predicate;
        };
    }

    private Set<BlogTag> resolveTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(this::findOrCreateTag)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private BlogTag findOrCreateTag(String name) {
        String slug = mapper.slugify(name);
        return tagRepository.findBySlugAndDeletedFlagFalse(slug).orElseGet(() -> {
            BlogTag tag = new BlogTag();
            tag.setName(name);
            tag.setSlug(slug);
            return tagRepository.save(tag);
        });
    }

    private void createRevision(Blog blog, String changeSummary) {
        int nextRevision = revisionRepository.findTopByBlogOrderByRevisionNumberDesc(blog)
                .map(revision -> revision.getRevisionNumber() + 1)
                .orElse(1);
        BlogRevision revision = new BlogRevision();
        revision.setBlog(blog);
        revision.setRevisionNumber(nextRevision);
        revision.setTitle(blog.getTitle());
        revision.setSlug(blog.getSlug());
        revision.setContentHtml(blog.getContentHtml());
        revision.setChangeSummary(changeSummary == null || changeSummary.isBlank() ? "Content saved." : changeSummary);
        revisionRepository.save(revision);
    }

    private void createApprovalRequest(Blog blog) {
        BlogApproval approval = new BlogApproval();
        approval.setBlog(blog);
        approval.setDecision(BlogApprovalDecision.PENDING);
        approval.setRemarks("Submitted for editorial review.");
        approvalRepository.save(approval);
    }

    private String uniqueCopySlug(String sourceSlug) {
        String base = sourceSlug + "-copy";
        String slug = base;
        int counter = 2;
        while (blogRepository.existsBySlugAndLanguageCodeAndDeletedFlagFalse(slug, "en")) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private String uniqueBlogSlug(String requestedSlug, String title, String languageCode, Long currentId) {
        String base = mapper.slugify(requestedSlug == null || requestedSlug.isBlank() ? title : requestedSlug);
        if (base.isBlank()) {
            base = "blog-post";
        }
        if (base.length() > 220) {
            base = base.substring(0, 220).replaceAll("-+$", "");
        }
        String lang = languageCode == null || languageCode.isBlank() ? "en" : languageCode.trim().toLowerCase();
        String slug = base;
        int counter = 2;
        while (currentId == null
                ? blogRepository.existsBySlugAndLanguageCodeAndDeletedFlagFalse(slug, lang)
                : blogRepository.existsBySlugAndLanguageCodeAndIdNotAndDeletedFlagFalse(slug, lang, currentId)) {
            String suffix = "-" + counter++;
            String trimmedBase = base.length() + suffix.length() > 240 ? base.substring(0, 240 - suffix.length()).replaceAll("-+$", "") : base;
            slug = trimmedBase + suffix;
        }
        return slug;
    }

    private void assertVersion(BlogForm form, Blog blog) {
        if (form.getId() != null && form.getVersion() != null && blog.getVersion() != null && !form.getVersion().equals(blog.getVersion())) {
            throw new OptimisticLockingFailureException("This blog post was updated by another user. Reload and try again.");
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
