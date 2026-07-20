package com.ecommerce.app.module.order.model;

import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customer_order_group")
public class CustomerOrderGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true, length = 40)
    private String orderGroupCode;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users customer;

    @Column(length = 150)
    private String guestName;

    @Column(length = 150)
    private String guestEmail;

    @Column(length = 50)
    private String guestPhone;

    @Column(length = 100)
    private String guestSessionId;

    @Column(name = "guest_checkout", nullable = false)
    private boolean guestCheckout = false;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "mobile_verification_required", nullable = false)
    private boolean mobileVerificationRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "mobile_verification_status", length = 30)
    private MobileVerificationStatus mobileVerificationStatus;

    @Column(name = "mobile_verified_at")
    private LocalDateTime mobileVerifiedAt;

    @Column(name = "checkout_session_id", length = 80)
    private String checkoutSessionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal packingTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderPaymentState paymentState = OrderPaymentState.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod paymentMethod;

    @Column(length = 50)
    private String statusSummary;

    @OneToMany(mappedBy = "orderGroup")
    private List<SalesOrder> salesOrders = new ArrayList<>();

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

    public String getOrderGroupCode() {
        return orderGroupCode;
    }

    public void setOrderGroupCode(String orderGroupCode) {
        this.orderGroupCode = orderGroupCode;
    }

    public Users getCustomer() {
        return customer;
    }

    public void setCustomer(Users customer) {
        this.customer = customer;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public String getGuestSessionId() {
        return guestSessionId;
    }

    public void setGuestSessionId(String guestSessionId) {
        this.guestSessionId = guestSessionId;
    }

    public boolean isGuestCheckout() {
        return guestCheckout;
    }

    public void setGuestCheckout(boolean guestCheckout) {
        this.guestCheckout = guestCheckout;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public boolean isMobileVerificationRequired() {
        return mobileVerificationRequired;
    }

    public void setMobileVerificationRequired(boolean mobileVerificationRequired) {
        this.mobileVerificationRequired = mobileVerificationRequired;
    }

    public MobileVerificationStatus getMobileVerificationStatus() {
        return mobileVerificationStatus;
    }

    public void setMobileVerificationStatus(MobileVerificationStatus mobileVerificationStatus) {
        this.mobileVerificationStatus = mobileVerificationStatus;
    }

    public LocalDateTime getMobileVerifiedAt() {
        return mobileVerifiedAt;
    }

    public void setMobileVerifiedAt(LocalDateTime mobileVerifiedAt) {
        this.mobileVerifiedAt = mobileVerifiedAt;
    }

    public String getCheckoutSessionId() {
        return checkoutSessionId;
    }

    public void setCheckoutSessionId(String checkoutSessionId) {
        this.checkoutSessionId = checkoutSessionId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(BigDecimal shippingTotal) {
        this.shippingTotal = shippingTotal;
    }

    public BigDecimal getPackingTotal() {
        return packingTotal;
    }

    public void setPackingTotal(BigDecimal packingTotal) {
        this.packingTotal = packingTotal;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public OrderPaymentState getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(OrderPaymentState paymentState) {
        this.paymentState = paymentState;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(String statusSummary) {
        this.statusSummary = statusSummary;
    }

    public List<SalesOrder> getSalesOrders() {
        return salesOrders;
    }

    public void setSalesOrders(List<SalesOrder> salesOrders) {
        this.salesOrders = salesOrders;
    }
}
