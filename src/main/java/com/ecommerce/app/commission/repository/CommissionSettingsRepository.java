package com.ecommerce.app.commission.repository;

import com.ecommerce.app.commission.model.MarketplaceCommissionSettings;
import com.ecommerce.app.commission.model.CommissionStatus;
import com.ecommerce.app.commission.model.CommissionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionSettingsRepository extends JpaRepository<MarketplaceCommissionSettings, Long> {

    /**
     * Find default commission setting
     */
    Optional<MarketplaceCommissionSettings> findFirstByCommissionTypeAndStatusOrderByIdDesc(
            CommissionType type,
            CommissionStatus status);

    /**
     * Find active commission by category
     */
    Optional<MarketplaceCommissionSettings> findByCategoryIdAndStatusOrderByIdDesc(
            Long categoryId,
            CommissionStatus status);

    /**
     * Find active commission by vendor
     */
    Optional<MarketplaceCommissionSettings> findByVendorIdAndStatusOrderByIdDesc(
            Long vendorId,
            CommissionStatus status);

    /**
     * Find active commission by product
     */
    Optional<MarketplaceCommissionSettings> findByProductIdAndStatusOrderByIdDesc(
            Long productId,
            CommissionStatus status);

    /**
     * Find all active commissions by type
     */
    List<MarketplaceCommissionSettings> findByCommissionTypeAndStatusOrderByCreatedAtDesc(
            CommissionType type,
            CommissionStatus status);

    /**
     * Find commissions with pagination
     */
    Page<MarketplaceCommissionSettings> findByStatusOrderByCreatedAtDesc(
            CommissionStatus status,
            Pageable pageable);

    Page<MarketplaceCommissionSettings> findByStatusNotOrderByCreatedAtDesc(
            CommissionStatus status,
            Pageable pageable);

    /**
     * Find commissions by type with pagination
     */
    Page<MarketplaceCommissionSettings> findByCommissionTypeAndStatusOrderByCreatedAtDesc(
            CommissionType type,
            CommissionStatus status,
            Pageable pageable);

    Page<MarketplaceCommissionSettings> findByCommissionTypeAndStatusNotOrderByCreatedAtDesc(
            CommissionType type,
            CommissionStatus status,
            Pageable pageable);

    long countByStatusNot(CommissionStatus status);

    long countByStatus(CommissionStatus status);

    long countByCommissionTypeAndStatus(CommissionType type, CommissionStatus status);

    /**
     * Custom query to check if commission setting exists
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM MarketplaceCommissionSettings cs WHERE cs.commissionType = :type "
            + "AND cs.categoryId = :categoryId AND cs.status = :status")
    boolean existsByCategoryAndType(
            @Param("type") CommissionType type,
            @Param("categoryId") Long categoryId,
            @Param("status") CommissionStatus status);

    /**
     * Custom query to check if vendor commission exists
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM MarketplaceCommissionSettings cs WHERE cs.commissionType = :type "
            + "AND cs.vendorId = :vendorId AND cs.status = :status")
    boolean existsByVendorAndType(
            @Param("type") CommissionType type,
            @Param("vendorId") Long vendorId,
            @Param("status") CommissionStatus status);

    /**
     * Find all active category commissions
     */
    List<MarketplaceCommissionSettings> findByCategoryIdAndStatusAndCommissionType(
            Long categoryId,
            CommissionStatus status,
            CommissionType type);

    /**
     * Find all for a vendor (both VENDOR and their applicable CATEGORY
     * commissions)
     */
    @Query("SELECT cs FROM MarketplaceCommissionSettings cs WHERE "
            + "(cs.vendorId = :vendorId AND cs.status = :status) "
            + "OR (cs.commissionType = :defaultType AND cs.status = :status)")
    List<MarketplaceCommissionSettings> findApplicableCommissionsForVendor(
            @Param("vendorId") Long vendorId,
            @Param("defaultType") CommissionType defaultType,
            @Param("status") CommissionStatus status);
}
