package com.ecommerce.app.commission.service;

import com.ecommerce.app.commission.model.CommissionStatus;
import com.ecommerce.app.commission.model.CommissionType;
import com.ecommerce.app.commission.model.MarketplaceCommissionSettings;
import com.ecommerce.app.commission.repository.CommissionSettingsRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommissionSettingsService {

    public static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("20.00");

    private final CommissionSettingsRepository commissionSettingsRepository;

    public CommissionSettingsService(CommissionSettingsRepository commissionSettingsRepository) {
        this.commissionSettingsRepository = commissionSettingsRepository;
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceCommissionSettings> findAllActive(Pageable pageable) {
        return commissionSettingsRepository.findByStatusNotOrderByCreatedAtDesc(CommissionStatus.ARCHIVED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceCommissionSettings> findByType(CommissionType type, Pageable pageable) {
        if (type == null) {
            return findAllActive(pageable);
        }
        return commissionSettingsRepository.findByCommissionTypeAndStatusNotOrderByCreatedAtDesc(
                type, CommissionStatus.ARCHIVED, pageable);
    }

    @Transactional(readOnly = true)
    public long countVisibleSettings() {
        return commissionSettingsRepository.countByStatusNot(CommissionStatus.ARCHIVED);
    }

    @Transactional(readOnly = true)
    public long countActiveSettings() {
        return commissionSettingsRepository.countByStatus(CommissionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countActiveSettingsByType(CommissionType type) {
        if (type == null) {
            return 0;
        }
        return commissionSettingsRepository.countByCommissionTypeAndStatus(type, CommissionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<MarketplaceCommissionSettings> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return commissionSettingsRepository.findById(id);
    }

    public MarketplaceCommissionSettings save(MarketplaceCommissionSettings commissionSetting) {
        MarketplaceCommissionSettings target = commissionSetting;
        if (commissionSetting != null && commissionSetting.getId() != null) {
            target = requireSetting(commissionSetting.getId());
            copyEditableFields(commissionSetting, target);
        }
        validateForSave(target);
        return commissionSettingsRepository.save(target);
    }

    public void delete(Long id) {
        MarketplaceCommissionSettings setting = requireSetting(id);
        commissionSettingsRepository.delete(setting);
    }

    public void deactivate(Long id) {
        MarketplaceCommissionSettings setting = requireSetting(id);
        setting.setStatus(CommissionStatus.INACTIVE);
        commissionSettingsRepository.save(setting);
    }

    public void activate(Long id) {
        MarketplaceCommissionSettings setting = requireSetting(id);
        setting.setStatus(CommissionStatus.ACTIVE);
        validateForSave(setting);
        commissionSettingsRepository.save(setting);
    }

    @Transactional(readOnly = true)
    public BigDecimal getApplicableCommissionRate(Long productId, Long vendorId, Long categoryId) {
        return resolveApplicable(productId, vendorId, categoryId)
                .map(MarketplaceCommissionSettings::getCommissionRate)
                .orElse(DEFAULT_COMMISSION_RATE);
    }

    @Transactional(readOnly = true)
    public BigDecimal getApplicableCommissionRateForCategory(Long categoryId) {
        return getApplicableCommissionRate(null, null, categoryId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getApplicableCommissionRateForVendor(Long vendorId) {
        return getApplicableCommissionRate(null, vendorId, null);
    }

    @Transactional(readOnly = true)
    public Optional<MarketplaceCommissionSettings> resolveApplicable(
            Long productId,
            Long vendorId,
            Long categoryId) {
        Optional<MarketplaceCommissionSettings> productRule = productId == null
                ? Optional.empty()
                : commissionSettingsRepository.findByProductIdAndStatusOrderByIdDesc(productId, CommissionStatus.ACTIVE);
        if (isUsable(productRule)) {
            return productRule;
        }

        Optional<MarketplaceCommissionSettings> vendorRule = vendorId == null
                ? Optional.empty()
                : commissionSettingsRepository.findByVendorIdAndStatusOrderByIdDesc(vendorId, CommissionStatus.ACTIVE);
        if (isUsable(vendorRule)) {
            return vendorRule;
        }

        Optional<MarketplaceCommissionSettings> categoryRule = categoryId == null
                ? Optional.empty()
                : commissionSettingsRepository.findByCategoryIdAndStatusOrderByIdDesc(categoryId, CommissionStatus.ACTIVE);
        if (isUsable(categoryRule)) {
            return categoryRule;
        }

        List<MarketplaceCommissionSettings> defaults
                = commissionSettingsRepository.findByCommissionTypeAndStatusOrderByCreatedAtDesc(
                        CommissionType.DEFAULT, CommissionStatus.ACTIVE);
        return defaults.stream()
                .filter(MarketplaceCommissionSettings::isCurrentlyApplicable)
                .findFirst();
    }

    private boolean isUsable(Optional<MarketplaceCommissionSettings> setting) {
        return setting.isPresent()
                && setting.get().isCurrentlyApplicable();
    }

    private MarketplaceCommissionSettings requireSetting(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commission setting not found."));
    }

    private void copyEditableFields(MarketplaceCommissionSettings source, MarketplaceCommissionSettings target) {
        target.setCommissionType(source.getCommissionType());
        target.setCommissionRate(source.getCommissionRate());
        target.setDescription(source.getDescription());
        target.setCategoryId(source.getCategoryId());
        target.setVendorId(source.getVendorId());
        target.setProductId(source.getProductId());
        target.setStatus(source.getStatus());
        target.setNotes(source.getNotes());
    }

    private void validateForSave(MarketplaceCommissionSettings setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Commission setting is required.");
        }
        if (setting.getCommissionType() == null) {
            throw new IllegalArgumentException("Commission type is required.");
        }
        if (setting.getCommissionRate() == null) {
            throw new IllegalArgumentException("Commission rate is required.");
        }
        if (setting.getCommissionRate().compareTo(BigDecimal.ZERO) < 0
                || setting.getCommissionRate().compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Commission rate must be between 0 and 100.");
        }
        if (setting.getStatus() == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        validateScope(setting);
        validateActiveDuplicate(setting);
    }

    private void validateScope(MarketplaceCommissionSettings setting) {
        switch (setting.getCommissionType()) {
            case DEFAULT -> {
                setting.setCategoryId(null);
                setting.setVendorId(null);
                setting.setProductId(null);
            }
            case CATEGORY -> {
                if (setting.getCategoryId() == null) {
                    throw new IllegalArgumentException("Category is required for category commission.");
                }
                setting.setVendorId(null);
                setting.setProductId(null);
            }
            case VENDOR -> {
                if (setting.getVendorId() == null) {
                    throw new IllegalArgumentException("Vendor is required for vendor commission.");
                }
                setting.setCategoryId(null);
                setting.setProductId(null);
            }
            case PRODUCT -> {
                if (setting.getProductId() == null) {
                    throw new IllegalArgumentException("Product is required for product commission.");
                }
                setting.setCategoryId(null);
                setting.setVendorId(null);
            }
        }
    }

    private void validateActiveDuplicate(MarketplaceCommissionSettings setting) {
        if (setting.getStatus() != CommissionStatus.ACTIVE) {
            return;
        }

        Optional<MarketplaceCommissionSettings> existing = switch (setting.getCommissionType()) {
            case DEFAULT -> commissionSettingsRepository.findFirstByCommissionTypeAndStatusOrderByIdDesc(
                    CommissionType.DEFAULT, CommissionStatus.ACTIVE);
            case CATEGORY -> commissionSettingsRepository.findByCategoryIdAndStatusOrderByIdDesc(
                    setting.getCategoryId(), CommissionStatus.ACTIVE);
            case VENDOR -> commissionSettingsRepository.findByVendorIdAndStatusOrderByIdDesc(
                    setting.getVendorId(), CommissionStatus.ACTIVE);
            case PRODUCT -> commissionSettingsRepository.findByProductIdAndStatusOrderByIdDesc(
                    setting.getProductId(), CommissionStatus.ACTIVE);
        };

        if (existing.isPresent() && !existing.get().getId().equals(setting.getId())) {
            throw new IllegalArgumentException("An active commission setting already exists for this scope.");
        }
    }
}
