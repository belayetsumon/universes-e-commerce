package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogBookmark;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogView;
import com.ecommerce.app.module.blog.repository.BlogBookmarkRepository;
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
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerBlogService {

    private final BlogRepository blogRepository;
    private final BlogBookmarkRepository bookmarkRepository;
    private final BlogViewRepository viewRepository;
    private final UsersRepository usersRepository;
    private final LoggedUserService loggedUserService;

    public CustomerBlogService(
            BlogRepository blogRepository,
            BlogBookmarkRepository bookmarkRepository,
            BlogViewRepository viewRepository,
            UsersRepository usersRepository,
            LoggedUserService loggedUserService) {
        this.blogRepository = blogRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.viewRepository = viewRepository;
        this.usersRepository = usersRepository;
        this.loggedUserService = loggedUserService;
    }

    @Transactional(readOnly = true)
    public Page<Blog> customerFeed(String query, Pageable pageable) {
        return blogRepository.publicSearch(BlogPublicationStatus.PUBLISHED, clean(query), null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Blog> findPublished(String slug, String languageCode) {
        return blogRepository.findBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(slug, clean(languageCode) == null ? "en" : languageCode.trim())
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
