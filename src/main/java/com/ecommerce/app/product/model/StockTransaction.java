package com.ecommerce.app.product.model;

import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.SalesOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product_stock_transaction",
        indexes = {
            @Index(name = "idx_product", columnList = "product_id"),
            @Index(name = "idx_catalog_variant", columnList = "catalog_variant_id"),
            @Index(name = "idx_created", columnList = "created"),
            @Index(name = "idx_idempotency", columnList = "idempotency_key")
        }
)
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "idempotency_key", nullable = false, unique = true, updatable = false)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductVariant catalogVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    private StockTransactionTypeEnum transactionType;

    @Enumerated(EnumType.STRING)
    private StockBucketEnum fromBucket;

    @Enumerated(EnumType.STRING)
    private StockBucketEnum toBucket;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal availableAfter;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal reservedAfter;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal soldAfter;

    private String note;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime created;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public StockTransaction(Long id, String uuid, String idempotencyKey, Product product, ProductVariant catalogVariant, SalesOrder salesOrder, OrderItem orderItem, StockTransactionTypeEnum transactionType, StockBucketEnum fromBucket, StockBucketEnum toBucket, BigDecimal quantity, BigDecimal availableAfter, BigDecimal reservedAfter, BigDecimal soldAfter, String note, String createdBy, LocalDateTime created) {
        this.id = id;
        this.uuid = uuid;
        this.idempotencyKey = idempotencyKey;
        this.product = product;
        this.catalogVariant = catalogVariant;
        this.salesOrder = salesOrder;
        this.orderItem = orderItem;
        this.transactionType = transactionType;
        this.fromBucket = fromBucket;
        this.toBucket = toBucket;
        this.quantity = quantity;
        this.availableAfter = availableAfter;
        this.reservedAfter = reservedAfter;
        this.soldAfter = soldAfter;
        this.note = note;
        this.createdBy = createdBy;
        this.created = created;
    }

    public StockTransaction() {
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductVariant getCatalogVariant() {
        return catalogVariant;
    }

    public void setCatalogVariant(ProductVariant catalogVariant) {
        this.catalogVariant = catalogVariant;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public StockTransactionTypeEnum getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(StockTransactionTypeEnum transactionType) {
        this.transactionType = transactionType;
    }

    public StockBucketEnum getFromBucket() {
        return fromBucket;
    }

    public void setFromBucket(StockBucketEnum fromBucket) {
        this.fromBucket = fromBucket;
    }

    public StockBucketEnum getToBucket() {
        return toBucket;
    }

    public void setToBucket(StockBucketEnum toBucket) {
        this.toBucket = toBucket;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvailableAfter() {
        return availableAfter;
    }

    public void setAvailableAfter(BigDecimal availableAfter) {
        this.availableAfter = availableAfter;
    }

    public BigDecimal getReservedAfter() {
        return reservedAfter;
    }

    public void setReservedAfter(BigDecimal reservedAfter) {
        this.reservedAfter = reservedAfter;
    }

    public BigDecimal getSoldAfter() {
        return soldAfter;
    }

    public void setSoldAfter(BigDecimal soldAfter) {
        this.soldAfter = soldAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

}
