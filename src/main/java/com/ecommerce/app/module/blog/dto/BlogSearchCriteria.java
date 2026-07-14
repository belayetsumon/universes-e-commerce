package com.ecommerce.app.module.blog.dto;

import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogVisibility;

public class BlogSearchCriteria {

    private String query;
    private BlogPublicationStatus status;
    private BlogVisibility visibility;
    private Long categoryId;
    private Long authorId;
    private String sort = "updatedAt";
    private String direction = "desc";

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public BlogPublicationStatus getStatus() {
        return status;
    }

    public void setStatus(BlogPublicationStatus status) {
        this.status = status;
    }

    public BlogVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(BlogVisibility visibility) {
        this.visibility = visibility;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
