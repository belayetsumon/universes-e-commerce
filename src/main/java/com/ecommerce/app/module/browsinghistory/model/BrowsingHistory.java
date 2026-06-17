package com.ecommerce.app.module.browsinghistory.model;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Productcategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_browsing_history")
public class BrowsingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false, length = 60)
    private String browserId;

    @Column(length = 120)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BrowsingHistoryViewType viewType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Productcategory category;

    @Column(length = 120)
    private String ipAddress;

    @Column(length = 1200)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    public BrowsingHistory() {
    }

    public BrowsingHistory(Long id, String uuid, Users user, String browserId, String sessionId, BrowsingHistoryViewType viewType, Product product, Productcategory category, String ipAddress, String userAgent, LocalDateTime viewedAt) {
        this.id = id;
        this.uuid = uuid;
        this.user = user;
        this.browserId = browserId;
        this.sessionId = sessionId;
        this.viewType = viewType;
        this.product = product;
        this.category = category;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.viewedAt = viewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getBrowserId() {
        return browserId;
    }

    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public BrowsingHistoryViewType getViewType() {
        return viewType;
    }

    public void setViewType(BrowsingHistoryViewType viewType) {
        this.viewType = viewType;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Productcategory getCategory() {
        return category;
    }

    public void setCategory(Productcategory category) {
        this.category = category;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
