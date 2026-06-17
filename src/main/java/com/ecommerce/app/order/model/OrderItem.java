/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.model;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Unitofmeasurement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@EntityListeners({AuditingEntityListener.class})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Sales order cannot be blank.")
    @ManyToOne(optional = true)
    public SalesOrder salesOrder;

    @NotNull(message = "product cannot be blank.")
    @ManyToOne(optional = true)
    public Product product;

    public Long vendorId;

    public Long productid;

    public String catalogVariantUuid;

    public String variantSummary;

    public Boolean preorder = Boolean.FALSE;

    public LocalDate preorderAvailableFrom;

    private String digitalAccessUrl;

    private String digitalLicenseCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String digitalDeliveryNote;

    private Boolean digitalDelivered = Boolean.FALSE;

    private LocalDateTime digitalDeliveredAt;

    @Enumerated(EnumType.STRING)
    private OrderItemReturnStatus returnStatus = OrderItemReturnStatus.NONE;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String returnRequestRemark;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String returnProcessedRemark;

    private LocalDateTime returnRequestedAt;

    private LocalDateTime returnedAt;

    private BigDecimal returnRefundAmount = BigDecimal.ZERO.setScale(2);

    public BigDecimal quantity = BigDecimal.ZERO.setScale(2);

    private Unitofmeasurement uom;
    public BigDecimal weight = BigDecimal.ZERO.setScale(2);
    public BigDecimal salesPrice = BigDecimal.ZERO.setScale(2);

    public BigDecimal discountRate = BigDecimal.ZERO.setScale(2);

    public BigDecimal discountAmount = BigDecimal.ZERO.setScale(2);

    public BigDecimal marketPlaceCommissionRate = BigDecimal.ZERO.setScale(2);

    public BigDecimal marketPlaceCommissionAmount = BigDecimal.ZERO.setScale(2);

    public BigDecimal vendorAmount = BigDecimal.ZERO.setScale(2);

    public BigDecimal vatRate = BigDecimal.ZERO.setScale(2);

    public BigDecimal vatAmount = BigDecimal.ZERO.setScale(2);

    public BigDecimal itemTotal = BigDecimal.ZERO.setScale(2);

    /// Audit ///
    @CreatedBy
    @Column(nullable = false, updatable = false)
    public String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    public String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    public LocalDateTime modified;

    /// End Audit ////
    public OrderItem() {
    }

    public OrderItem(Long id, SalesOrder salesOrder, Product product, Long vendorId, Long productid, BigDecimal quantity, Unitofmeasurement uom, BigDecimal weight, BigDecimal salesPrice, BigDecimal discountRate, BigDecimal discountAmount, BigDecimal marketPlaceCommissionRate, BigDecimal marketPlaceCommissionAmount, BigDecimal vendorAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal itemTotal, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.salesOrder = salesOrder;
        this.product = product;
        this.vendorId = vendorId;
        this.productid = productid;
        this.quantity = quantity;
        this.uom = uom;
        this.weight = weight;
        this.salesPrice = salesPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.marketPlaceCommissionRate = marketPlaceCommissionRate;
        this.marketPlaceCommissionAmount = marketPlaceCommissionAmount;
        this.vendorAmount = vendorAmount;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.itemTotal = itemTotal;
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

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getProductid() {
        return productid;
    }

    public void setProductid(Long productid) {
        this.productid = productid;
    }

    public String getCatalogVariantUuid() {
        return catalogVariantUuid;
    }

    public void setCatalogVariantUuid(String catalogVariantUuid) {
        this.catalogVariantUuid = catalogVariantUuid;
    }

    public String getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(String variantSummary) {
        this.variantSummary = variantSummary;
    }

    public Boolean getPreorder() {
        return preorder;
    }

    public void setPreorder(Boolean preorder) {
        this.preorder = preorder;
    }

    public LocalDate getPreorderAvailableFrom() {
        return preorderAvailableFrom;
    }

    public void setPreorderAvailableFrom(LocalDate preorderAvailableFrom) {
        this.preorderAvailableFrom = preorderAvailableFrom;
    }

    public String getDigitalAccessUrl() {
        return digitalAccessUrl;
    }

    public void setDigitalAccessUrl(String digitalAccessUrl) {
        this.digitalAccessUrl = digitalAccessUrl;
    }

    public String getDigitalLicenseCode() {
        return digitalLicenseCode;
    }

    public void setDigitalLicenseCode(String digitalLicenseCode) {
        this.digitalLicenseCode = digitalLicenseCode;
    }

    public String getDigitalDeliveryNote() {
        return digitalDeliveryNote;
    }

    public void setDigitalDeliveryNote(String digitalDeliveryNote) {
        this.digitalDeliveryNote = digitalDeliveryNote;
    }

    public Boolean getDigitalDelivered() {
        return digitalDelivered;
    }

    public void setDigitalDelivered(Boolean digitalDelivered) {
        this.digitalDelivered = digitalDelivered;
    }

    public LocalDateTime getDigitalDeliveredAt() {
        return digitalDeliveredAt;
    }

    public void setDigitalDeliveredAt(LocalDateTime digitalDeliveredAt) {
        this.digitalDeliveredAt = digitalDeliveredAt;
    }

    public OrderItemReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(OrderItemReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnRequestRemark() {
        return returnRequestRemark;
    }

    public void setReturnRequestRemark(String returnRequestRemark) {
        this.returnRequestRemark = returnRequestRemark;
    }

    public String getReturnProcessedRemark() {
        return returnProcessedRemark;
    }

    public void setReturnProcessedRemark(String returnProcessedRemark) {
        this.returnProcessedRemark = returnProcessedRemark;
    }

    public LocalDateTime getReturnRequestedAt() {
        return returnRequestedAt;
    }

    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) {
        this.returnRequestedAt = returnRequestedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public BigDecimal getReturnRefundAmount() {
        return returnRefundAmount;
    }

    public void setReturnRefundAmount(BigDecimal returnRefundAmount) {
        this.returnRefundAmount = returnRefundAmount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Unitofmeasurement getUom() {
        return uom;
    }

    public void setUom(Unitofmeasurement uom) {
        this.uom = uom;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getMarketPlaceCommissionRate() {
        return marketPlaceCommissionRate;
    }

    public void setMarketPlaceCommissionRate(BigDecimal marketPlaceCommissionRate) {
        this.marketPlaceCommissionRate = marketPlaceCommissionRate;
    }

    public BigDecimal getMarketPlaceCommissionAmount() {
        return marketPlaceCommissionAmount;
    }

    public void setMarketPlaceCommissionAmount(BigDecimal marketPlaceCommissionAmount) {
        this.marketPlaceCommissionAmount = marketPlaceCommissionAmount;
    }

    public BigDecimal getVendorAmount() {
        return vendorAmount;
    }

    public void setVendorAmount(BigDecimal vendorAmount) {
        this.vendorAmount = vendorAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
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
