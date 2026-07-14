package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogCommentRepository;
import com.ecommerce.app.module.blog.repository.BlogFaqRepository;
import com.ecommerce.app.module.blog.repository.BlogMediaRepository;
import com.ecommerce.app.module.blog.repository.BlogRelatedProductRepository;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogPublicService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogMediaRepository mediaRepository;
    private final BlogRelatedProductRepository relatedProductRepository;
    private final BlogFaqRepository faqRepository;
    private final BlogEngagementService engagementService;

    public BlogPublicService(
            BlogRepository blogRepository,
            BlogCategoryRepository categoryRepository,
            BlogCommentRepository commentRepository,
            BlogMediaRepository mediaRepository,
            BlogRelatedProductRepository relatedProductRepository,
            BlogFaqRepository faqRepository,
            BlogEngagementService engagementService) {
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.mediaRepository = mediaRepository;
        this.relatedProductRepository = relatedProductRepository;
        this.faqRepository = faqRepository;
        this.engagementService = engagementService;
    }

    @Transactional(readOnly = true)
    public Page<Blog> publicPosts(String query, String categorySlug, String authorSlug, Pageable pageable) {
        return blogRepository.publicSearch(BlogPublicationStatus.PUBLISHED, clean(query), clean(categorySlug), clean(authorSlug), pageable);
    }

    @Transactional
    public Optional<Blog> findPublishedBySlug(String slug, String languageCode, HttpServletRequest request) {
        Optional<Blog> blog = blogRepository.findBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(slug, languageCode == null ? "en" : languageCode);
        blog.filter(this::isPubliclyVisible).ifPresent(post -> engagementService.recordView(post, request));
        return blog.filter(this::isPubliclyVisible);
    }

    @Transactional(readOnly = true)
    public void enrichArticleModel(org.springframework.ui.Model model, Blog blog) {
        model.addAttribute("comments", commentRepository.findByBlogAndModerationStatusAndDeletedFlagFalseOrderByCreatedAtAsc(
                blog, com.ecommerce.app.module.blog.model.BlogModerationStatus.APPROVED));
        model.addAttribute("mediaItems", mediaRepository.findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(blog));
        model.addAttribute("relatedProducts", relatedProductRepository.findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(blog));
        model.addAttribute("faqs", faqRepository.findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(blog));
        model.addAttribute("popularArticles", blogRepository.findTop8ByStatusAndDeletedFlagFalseAndActiveFlagTrueOrderByViewCountDescPublishedAtDesc(BlogPublicationStatus.PUBLISHED));
        model.addAttribute("relatedArticles", blogRepository.findTop8ByStatusAndPublishedAtLessThanEqualAndDeletedFlagFalseAndActiveFlagTrueOrderByPublishedAtDesc(BlogPublicationStatus.PUBLISHED, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public void enrichListingModel(org.springframework.ui.Model model) {
        model.addAttribute("blogCategories", categoryRepository.findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscNameAsc());
        model.addAttribute("featuredArticles", blogRepository.findTop8ByStatusAndFeaturedPostTrueAndDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscPublishedAtDesc(BlogPublicationStatus.PUBLISHED));
        model.addAttribute("popularArticles", blogRepository.findTop8ByStatusAndDeletedFlagFalseAndActiveFlagTrueOrderByViewCountDescPublishedAtDesc(BlogPublicationStatus.PUBLISHED));
    }

    private boolean isPubliclyVisible(Blog blog) {
        return blog.getStatus() == BlogPublicationStatus.PUBLISHED
                && blog.isActiveFlag()
                && !blog.isDeletedFlag()
                && (blog.getPublishedAt() == null || !blog.getPublishedAt().isAfter(LocalDateTime.now()))
                && (blog.getExpiresAt() == null || blog.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
