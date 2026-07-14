package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.dto.BlogCommentForm;
import com.ecommerce.app.module.blog.dto.BlogSubscriberForm;
import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogComment;
import com.ecommerce.app.module.blog.model.BlogModerationStatus;
import com.ecommerce.app.module.blog.model.BlogSubscriber;
import com.ecommerce.app.module.blog.model.BlogSubscriberStatus;
import com.ecommerce.app.module.blog.model.BlogView;
import com.ecommerce.app.module.blog.repository.BlogCommentRepository;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import com.ecommerce.app.module.blog.repository.BlogSubscriberRepository;
import com.ecommerce.app.module.blog.repository.BlogViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogEngagementService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogSubscriberRepository subscriberRepository;
    private final BlogViewRepository viewRepository;

    public BlogEngagementService(
            BlogRepository blogRepository,
            BlogCommentRepository commentRepository,
            BlogSubscriberRepository subscriberRepository,
            BlogViewRepository viewRepository) {
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.subscriberRepository = subscriberRepository;
        this.viewRepository = viewRepository;
    }

    @Transactional
    public void recordView(Blog blog, HttpServletRequest request) {
        BlogView view = new BlogView();
        view.setBlog(blog);
        view.setVisitorKey(hash(request.getRemoteAddr() + "|" + request.getHeader("User-Agent")));
        view.setUtmCampaign(request.getParameter("utm_campaign"));
        viewRepository.save(view);
        blog.setViewCount(blog.getViewCount() == null ? 1L : blog.getViewCount() + 1L);
        blogRepository.save(blog);
    }

    @Transactional
    public void submitComment(Blog blog, BlogCommentForm form, HttpServletRequest request) {
        if (!blog.isAllowComments()) {
            throw new IllegalStateException("Comments are closed for this article.");
        }
        BlogComment comment = new BlogComment();
        comment.setBlog(blog);
        if (form.getParentId() != null) {
            commentRepository.findById(form.getParentId()).ifPresent(comment::setParent);
        }
        comment.setGuestName(form.getName().trim());
        comment.setGuestEmail(form.getEmail() == null ? null : form.getEmail().trim().toLowerCase());
        comment.setCommentText(sanitizeComment(form.getComment()));
        comment.setIpHash(hash(request.getRemoteAddr()));
        comment.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        comment.setModerationStatus(BlogModerationStatus.PENDING);
        commentRepository.save(comment);
        blog.setCommentCount(blog.getCommentCount() == null ? 1L : blog.getCommentCount() + 1L);
        blogRepository.save(blog);
    }

    @Transactional
    public void subscribe(BlogSubscriberForm form) {
        String email = form.getEmail().trim().toLowerCase();
        BlogSubscriber subscriber = subscriberRepository.findByEmail(email).orElseGet(BlogSubscriber::new);
        subscriber.setEmail(email);
        subscriber.setName(form.getName() == null ? null : form.getName().trim());
        subscriber.setSubscriberStatus(BlogSubscriberStatus.SUBSCRIBED);
        subscriber.setActiveFlag(true);
        subscriber.setConsentAt(LocalDateTime.now());
        subscriber.setSource("PUBLIC_BLOG");
        subscriberRepository.save(subscriber);
    }

    private String sanitizeComment(String comment) {
        return comment == null ? "" : comment.replaceAll("<[^>]+>", "").trim();
    }

    private String truncate(String value, int length) {
        if (value == null || value.length() <= length) {
            return value;
        }
        return value.substring(0, length);
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
