package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.dto.BlogForm;
import com.ecommerce.app.module.blog.mapper.BlogMapper;
import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogBookmark;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogVisibility;
import com.ecommerce.app.module.blog.model.BlogView;
import com.ecommerce.app.module.blog.repository.BlogBookmarkRepository;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import com.ecommerce.app.module.blog.repository.BlogViewRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerBlogService {

    private final BlogRepository blogRepository;
    private final BlogBookmarkRepository bookmarkRepository;
    private final BlogViewRepository viewRepository;
    private final BlogCategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final LoggedUserService loggedUserService;
    private final BlogMapper mapper;
    private final BlogHtmlSanitizer htmlSanitizer;
    private final BlogImageStorageService imageStorageService;

    public CustomerBlogService(
            BlogRepository blogRepository,
            BlogBookmarkRepository bookmarkRepository,
            BlogViewRepository viewRepository,
            BlogCategoryRepository categoryRepository,
            UsersRepository usersRepository,
            LoggedUserService loggedUserService,
            BlogMapper mapper,
            BlogHtmlSanitizer htmlSanitizer,
            BlogImageStorageService imageStorageService) {
        this.blogRepository = blogRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.viewRepository = viewRepository;
        this.categoryRepository = categoryRepository;
        this.usersRepository = usersRepository;
        this.loggedUserService = loggedUserService;
        this.mapper = mapper;
        this.htmlSanitizer = htmlSanitizer;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public Page<Blog> customerFeed(String query, Pageable pageable) {
        return blogRepository.publicSearch(BlogPublicationStatus.PUBLISHED, likePattern(query), null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Blog> myPosts(Pageable pageable) {
        return blogRepository.findByCreatedByAndDeletedFlagFalseOrderByUpdatedAtDesc(customerIdentity(activeUser()), pageable);
    }

    @Transactional
    public Blog submitCustomerPost(BlogForm form) {
        return submitCustomerPost(form, null);
    }

    @Transactional
    public Blog submitCustomerPost(BlogForm form, MultipartFile featuredImageFile) {
        Users user = activeUser();
        String owner = customerIdentity(user);
        Blog blog = new Blog();
        form.setStatus(BlogPublicationStatus.IN_REVIEW);
        form.setVisibility(BlogVisibility.PUBLIC);
        form.setLanguageCode(form.getLanguageCode() == null || form.getLanguageCode().isBlank() ? "en" : form.getLanguageCode());
        form.setSlug(uniqueBlogSlug(form.getSlug(), form.getTitle(), form.getLanguageCode()));
        applyFeaturedImageUpload(form, featuredImageFile);
        form.setStickyPost(false);
        form.setFeaturedPost(false);
        form.setAllowComments(true);
        form.setContentHtml(htmlSanitizer.sanitize(form.getContentHtml()));
        mapper.copyFormToBlog(form, blog);
        blog.setCategory(form.getCategoryId() == null ? null : categoryRepository.findById(form.getCategoryId()).orElse(null));
        blog.setCreatedBy(owner);
        blog.setUpdatedBy(owner);
        blog.setActiveFlag(true);
        blog.setDeletedFlag(false);
        return blogRepository.save(blog);
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

    @Transactional(readOnly = true)
    public Optional<Blog> findPublished(String slug, String languageCode) {
        return blogRepository.findBySlugAndLanguageCodeAndDeletedFlagFalse(cleanLower(slug), cleanLower(languageCode) == null ? "en" : cleanLower(languageCode))
                .filter(this::isReadable);
    }

    @Transactional
    public void recordCustomerView(Blog blog, HttpServletRequest request) {
        Users user = activeUser();
        BlogView view = new BlogView();
        view.setBlog(blog);
        view.setUser(user);
        view.setVisitorKey(hash(user.getId() + "|" + request.getRemoteAddr() + "|" + request.getHeader("User-Agent")));
        view.setUtmCampaign(request.getParameter("utm_campaign"));
        viewRepository.save(view);
        blog.setViewCount(blog.getViewCount() == null ? 1L : blog.getViewCount() + 1L);
        blogRepository.save(blog);
    }

    @Transactional(readOnly = true)
    public List<BlogBookmark> savedArticles() {
        return bookmarkRepository.findByUserAndDeletedFlagFalseAndActiveFlagTrueOrderByCreatedAtDesc(activeUser());
    }

    @Transactional(readOnly = true)
    public List<BlogView> recentlyViewed() {
        return viewRepository.findTop30ByUserOrderByViewedAtDesc(activeUser());
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Blog blog) {
        return bookmarkRepository.existsByBlogAndUserAndDeletedFlagFalseAndActiveFlagTrue(blog, activeUser());
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> bookmarkMap(Page<Blog> blogs) {
        Users user = activeUser();
        Map<Long, Boolean> bookmarks = new LinkedHashMap<>();
        blogs.forEach(blog -> bookmarks.put(blog.getId(), bookmarkRepository.existsByBlogAndUserAndDeletedFlagFalseAndActiveFlagTrue(blog, user)));
        return bookmarks;
    }

    @Transactional
    public void bookmark(Blog blog) {
        Users user = activeUser();
        BlogBookmark bookmark = bookmarkRepository.findByBlogAndUser(blog, user).orElseGet(BlogBookmark::new);
        bookmark.setBlog(blog);
        bookmark.setUser(user);
        bookmark.setFavoriteFlag(true);
        bookmark.setActiveFlag(true);
        bookmark.setDeletedFlag(false);
        bookmark.setDeletedAt(null);
        bookmark.setDeletedBy(null);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Blog blog) {
        Users user = activeUser();
        bookmarkRepository.findByBlogAndUser(blog, user).ifPresent(bookmark -> {
            bookmark.setFavoriteFlag(false);
            bookmark.setActiveFlag(false);
            bookmark.setDeletedFlag(true);
            bookmark.setDeletedAt(LocalDateTime.now());
            bookmark.setDeletedBy(user.getEmail());
            bookmarkRepository.save(bookmark);
        });
    }

    private Users activeUser() {
        Long userId = loggedUserService.activeUserid();
        return usersRepository.findById(userId).orElseThrow(() -> new IllegalStateException("Active customer account was not found."));
    }

    private String customerIdentity(Users user) {
        String identity = clean(user.getEmail());
        if (identity == null) {
            identity = clean(user.getMobile());
        }
        if (identity == null) {
            identity = "customer-" + user.getId();
        }
        return identity.length() > 100 ? identity.substring(0, 100) : identity;
    }

    private String uniqueBlogSlug(String requestedSlug, String title, String languageCode) {
        String base = mapper.slugify(requestedSlug == null || requestedSlug.isBlank() ? title : requestedSlug);
        if (base.isBlank()) {
            base = "customer-post";
        }
        if (base.length() > 220) {
            base = base.substring(0, 220).replaceAll("-+$", "");
        }
        String lang = languageCode == null || languageCode.isBlank() ? "en" : languageCode.trim().toLowerCase();
        String slug = base;
        int counter = 2;
        while (blogRepository.existsBySlugAndLanguageCodeAndDeletedFlagFalse(slug, lang)) {
            String suffix = "-" + counter++;
            String trimmedBase = base.length() + suffix.length() > 240 ? base.substring(0, 240 - suffix.length()).replaceAll("-+$", "") : base;
            slug = trimmedBase + suffix;
        }
        return slug;
    }

    private boolean isReadable(Blog blog) {
        return blog.getStatus() == BlogPublicationStatus.PUBLISHED
                && blog.isActiveFlag()
                && !blog.isDeletedFlag()
                && (blog.getPublishedAt() == null || !blog.getPublishedAt().isAfter(LocalDateTime.now()))
                && (blog.getExpiresAt() == null || blog.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanLower(String value) {
        String cleanValue = clean(value);
        return cleanValue == null ? null : cleanValue.toLowerCase(Locale.ROOT);
    }

    private String likePattern(String value) {
        String cleanValue = clean(value);
        return cleanValue == null ? null : "%" + cleanValue + "%";
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString((value == null ? "" : value).hashCode());
        }
    }
}
