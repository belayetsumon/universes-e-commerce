/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author User
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = " User cannot be blank.")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Users userId;

    @NotNull(message = "Exam  cannot be blank.")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Product product;
    @Lob

    private String comment;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    private CommentsStatus status;

    /// Audit /// 
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modified;

    public Comment() {
    }

    public Comment(Long id, Users userId, Product product, String comment, CommentsStatus status, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.comment = comment;
        this.status = status;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CommentsStatus getStatus() {
        return status;
    }

    public void setStatus(CommentsStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

   
}
