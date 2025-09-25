/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.repository;

import com.ecommerce.app.vendor.model.VendorTransaction;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
//public interface VendorTransactionRepository extends JpaRepository<VendorTransaction, Long> {
//
//    List<VendorTransaction> findByVendor_IdAndStatus(Long vendorId, VendorTransactionStatusEnum status);
//
//    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorTransaction v WHERE v.vendor.id = :vendorId AND v.status = 'PENDING'")
//    BigDecimal sumPendingAmount(Long vendorId);
//
//    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorTransaction v WHERE v.vendor.id = :vendorId AND v.status = 'PAID'")
//    BigDecimal sumPaidAmount(Long vendorId);
//
//    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorTransaction v WHERE v.vendor.id = :vendorId AND v.status = 'AVAILABLE'")
//    BigDecimal sumAvailableAmountByVendor(Long vendorId);
//
//    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorTransaction v WHERE v.vendor.id = :vendorId AND v.status = 'AVAILABLE'")
//    BigDecimal sumAvailableAmount(Long vendorId);
//
//    @Modifying
//    @Query("UPDATE VendorTransaction v SET v.status = 'PAID' WHERE v.vendor.id = :vendorId AND v.status = 'AVAILABLE'")
//    void markAvailableAsPaid(Long vendorId);
//}
public interface VendorTransactionRepository extends JpaRepository<VendorTransaction, Long> {

    List<VendorTransaction> findByVendor_IdAndStatus(Long vendorId, VendorTransactionStatusEnum status);

    @Query("""
        SELECT COALESCE(SUM(v.amount), 0)
        FROM VendorTransaction v
        WHERE v.vendor.id = :vendorId AND v.status = :status
    """)
    BigDecimal sumAmountByStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") VendorTransactionStatusEnum status
    );

    // If you still want separate methods for clarity:
    default BigDecimal sumPendingAmount(Long vendorId) {
        return sumAmountByStatus(vendorId, VendorTransactionStatusEnum.PENDING);
    }

    default BigDecimal sumPaidAmount(Long vendorId) {
        return sumAmountByStatus(vendorId, VendorTransactionStatusEnum.PAID);
    }

    default BigDecimal sumAvailableAmount(Long vendorId) {
        return sumAmountByStatus(vendorId, VendorTransactionStatusEnum.AVAILABLE);
    }

    @Modifying
    @Query("""
        UPDATE VendorTransaction v
        SET v.status = :newStatus
        WHERE v.vendor.id = :vendorId AND v.status = :oldStatus
    """)
    void markStatusAs(
            @Param("vendorId") Long vendorId,
            @Param("oldStatus") VendorTransactionStatusEnum oldStatus,
            @Param("newStatus") VendorTransactionStatusEnum newStatus
    );

    // Optional convenience method:
    default void markAvailableAsPaid(Long vendorId) {
        markStatusAs(
                vendorId,
                VendorTransactionStatusEnum.AVAILABLE,
                VendorTransactionStatusEnum.PAID
        );
    }

}
