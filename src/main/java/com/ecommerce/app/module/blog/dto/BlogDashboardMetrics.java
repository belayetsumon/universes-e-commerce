package com.ecommerce.app.module.blog.dto;

public class BlogDashboardMetrics {

    private final long totalPosts;
    private final long publishedPosts;
    private final long draftPosts;
    private final long reviewPosts;
    private final long scheduledPosts;
    private final long pendingComments;

    public BlogDashboardMetrics(long totalPosts, long publishedPosts, long draftPosts, long reviewPosts, long scheduledPosts, long pendingComments) {
        this.totalPosts = totalPosts;
        this.publishedPosts = publishedPosts;
        this.draftPosts = draftPosts;
        this.reviewPosts = reviewPosts;
        this.scheduledPosts = scheduledPosts;
        this.pendingComments = pendingComments;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public long getPublishedPosts() {
        return publishedPosts;
    }

    public long getDraftPosts() {
        return draftPosts;
    }

    public long getReviewPosts() {
        return reviewPosts;
    }

    public long getScheduledPosts() {
        return scheduledPosts;
    }

    public long getPendingComments() {
        return pendingComments;
    }
}
