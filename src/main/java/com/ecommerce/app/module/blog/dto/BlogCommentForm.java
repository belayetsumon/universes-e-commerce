package com.ecommerce.app.module.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BlogCommentForm {

    private Long parentId;

    @NotBlank(message = "Name is required.")
    @Size(max = 120, message = "Name cannot exceed 120 characters.")
    private String name;

    @Email(message = "Enter a valid email.")
    @Size(max = 180, message = "Email cannot exceed 180 characters.")
    private String email;

    @NotBlank(message = "Comment is required.")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters.")
    private String comment;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
