package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponScope;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponType;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.product.ripository.ManufacturerRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/coupon")
public class CouponController {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final ProductcategoryRepository productcategoryRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final VendorprofileRepository vendorprofileRepository;
    private final UsersRepository usersRepository;

    public CouponController(CouponRepository couponRepository, ProductRepository productRepository,
            ProductcategoryRepository productcategoryRepository, ManufacturerRepository manufacturerRepository,
            VendorprofileRepository vendorprofileRepository, UsersRepository usersRepository) {
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.productcategoryRepository = productcategoryRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.vendorprofileRepository = vendorprofileRepository;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("coupons", couponRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("coupons", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading coupons: " + ex.getMessage());
        }
        return "admin/referral_rewards/coupon-list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("coupon")) {
            Coupon coupon = new Coupon();
            LocalDateTime now = LocalDateTime.now();
            coupon.setStatus(CouponStatus.ACTIVE);
            coupon.setType(CouponType.FIXED);
            coupon.setScope(CouponScope.GLOBAL);
            coupon.setPublicCoupon(true);
            coupon.setRequiresCode(true);
            coupon.setAutoApply(false);
            coupon.setGuestAllowed(false);
            coupon.setDeleted(false);
            coupon.setPriority(0);
            coupon.setStartDate(now);
            coupon.setExpiryDate(now.plusDays(30));
            model.addAttribute("coupon", coupon);
        }
        addCouponFormOptions(model);
        return "admin/referral_rewards/coupon-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("coupon") Coupon coupon, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            validateCouponForSave(coupon, bindingResult);
            if (bindingResult.hasErrors()) {
                model.addAttribute("errorMessage", "Coupon could not be saved. Please review the highlighted errors.");
                addCouponFormOptions(model);
                return "admin/referral_rewards/coupon-form";
            }
            normalizeCoupon(coupon);
            Coupon couponToSave = resolveCouponForSave(coupon);
            couponRepository.save(couponToSave);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon saved successfully.");
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("errorMessage", "Coupon code must be unique.");
            addCouponFormOptions(model);
            return "admin/referral_rewards/coupon-form";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "Runtime error while saving coupon: " + ex.getMessage());
            addCouponFormOptions(model);
            return "admin/referral_rewards/coupon-form";
        }
        return "redirect:/coupon/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Coupon not found.");
            return "redirect:/coupon/list";
        }
        model.addAttribute("coupon", coupon.get());
        addCouponFormOptions(model);
        return "admin/referral_rewards/coupon-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!couponRepository.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Coupon not found.");
                return "redirect:/coupon/list";
            }
            Coupon coupon = couponRepository.findById(id).orElseThrow();
            coupon.setDeleted(true);
            coupon.setStatus(CouponStatus.DISABLED);
            couponRepository.save(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon disabled successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete coupon because it is linked to redemptions.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting coupon: " + ex.getMessage());
        }
        return "redirect:/coupon/list";
    }

    private void addCouponFormOptions(Model model) {
        Coupon currentCoupon = model.containsAttribute("coupon") ? (Coupon) model.getAttribute("coupon") : null;
        model.addAttribute("couponTypes", Arrays.asList(CouponType.values()));
        model.addAttribute("couponStatuses", Arrays.asList(CouponStatus.values()));
        model.addAttribute("couponScopes", availableCouponScopes(currentCoupon));
        model.addAttribute("couponProducts", productRepository.findAll());
        model.addAttribute("couponCategories", productcategoryRepository.findAll());
        model.addAttribute("couponBrands", manufacturerRepository.findAll());
        model.addAttribute("couponVendors", vendorprofileRepository.findAll());
        model.addAttribute("couponCustomers", usersRepository.findAll());
        model.addAttribute("couponCampaignKeys", Arrays.asList(
                "SEASONAL",
                "FLASH_SALE",
                "NEW_ARRIVAL",
                "CLEARANCE",
                "LOYALTY",
                "REFERRAL",
                "CHECKOUT"));
    }

    private void normalizeCoupon(Coupon coupon) {
        if (coupon == null) {
            return;
        }
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().trim().toUpperCase());
        }
        coupon.setVendorScope(normalizeDelimitedValues(coupon.getVendorScope()));
        coupon.setCampaignScope(normalizeDelimitedValues(coupon.getCampaignScope()));
        if (coupon.getScope() == null) {
            coupon.setScope(CouponScope.GLOBAL);
        }
        if (coupon.getPriority() == null) {
            coupon.setPriority(0);
        }
        if (coupon.getAutoApply() == null) {
            coupon.setAutoApply(false);
        }
        if (coupon.getGuestAllowed() == null) {
            coupon.setGuestAllowed(false);
        }
        if (coupon.getPublicCoupon() == null) {
            coupon.setPublicCoupon(true);
        }
        if (coupon.getRequiresCode() == null) {
            coupon.setRequiresCode(true);
        }
        if (coupon.getDeleted() == null) {
            coupon.setDeleted(false);
        }
    }

    private void validateCouponForSave(Coupon coupon, BindingResult bindingResult) {
        if (coupon == null) {
            bindingResult.reject("coupon.required", "Coupon information is required.");
            return;
        }
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            bindingResult.rejectValue("code", "coupon.code.required", "Coupon code is required.");
        }
        if (coupon.getTitle() == null || coupon.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "coupon.title.required", "Coupon title is required.");
        }
        if (coupon.getValue() == null) {
            bindingResult.rejectValue("value", "coupon.value.required", "Coupon value is required.");
        }
        if (coupon.getType() == null) {
            bindingResult.rejectValue("type", "coupon.type.required", "Coupon type is required.");
        }
        if (coupon.getStatus() == null) {
            bindingResult.rejectValue("status", "coupon.status.required", "Coupon status is required.");
        }
        if (coupon.getScope() == CouponScope.VENDOR
                && (coupon.getVendorScope() == null || coupon.getVendorScope().trim().isEmpty())) {
            bindingResult.rejectValue("vendorScope", "coupon.vendorScope.required", "Vendor scope target is required for vendor coupons.");
        }
        if (coupon.getScope() != null && coupon.getScope() != CouponScope.GLOBAL && coupon.getScope() != CouponScope.VENDOR
                && (coupon.getCampaignScope() == null || coupon.getCampaignScope().trim().isEmpty())) {
            bindingResult.rejectValue("campaignScope", "coupon.campaignScope.required", "Scope target is required for the selected coupon scope.");
        }
        if (coupon.getStartDate() != null && coupon.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("startDate", "coupon.startDate.past", "Start date cannot be before the current date.");
        }
        if (coupon.getStartDate() != null && coupon.getExpiryDate() != null
                && coupon.getExpiryDate().isBefore(coupon.getStartDate())) {
            bindingResult.rejectValue("expiryDate", "coupon.expiry.beforeStart", "End date and time must be after start date and time.");
        }
        if (coupon.getCode() != null && !coupon.getCode().trim().isEmpty()) {
            String normalizedCode = coupon.getCode().trim().toUpperCase();
            Long currentId = coupon.getId() == null ? 0L : coupon.getId();
            if (couponRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, currentId)) {
                bindingResult.rejectValue("code", "coupon.code.unique", "Coupon code must be unique.");
            }
        }
    }

    private Coupon resolveCouponForSave(Coupon formCoupon) {
        if (formCoupon.getId() == null) {
            return formCoupon;
        }

        Coupon existingCoupon = couponRepository.findById(formCoupon.getId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));
        copyEditableCouponFields(formCoupon, existingCoupon);
        return existingCoupon;
    }

    private void copyEditableCouponFields(Coupon source, Coupon target) {
        target.setTitle(source.getTitle());
        target.setCode(source.getCode());
        target.setDescription(source.getDescription());
        target.setType(source.getType());
        target.setValue(source.getValue());
        target.setMaxDiscount(source.getMaxDiscount());
        target.setMinimumOrder(source.getMinimumOrder());
        target.setStartDate(source.getStartDate());
        target.setExpiryDate(source.getExpiryDate());
        target.setUsageLimit(source.getUsageLimit());
        target.setPerUserUsageLimit(source.getPerUserUsageLimit());
        target.setTimesUsed(source.getTimesUsed());
        target.setStackable(source.isStackable());
        target.setPriority(source.getPriority());
        target.setAutoApply(source.getAutoApply());
        target.setNewCustomerOnly(source.isNewCustomerOnly());
        target.setFirstOrderOnly(source.isFirstOrderOnly());
        target.setGuestAllowed(source.getGuestAllowed());
        target.setVendorScope(source.getVendorScope());
        target.setCampaignScope(source.getCampaignScope());
        target.setScope(source.getScope());
        target.setPublicCoupon(source.getPublicCoupon());
        target.setRequiresCode(source.getRequiresCode());
        target.setStatus(source.getStatus());
        target.setMaxUsesPerIP(source.getMaxUsesPerIP());
        target.setMaxUsesPerDevice(source.getMaxUsesPerDevice());
        target.setDeleted(source.getDeleted());
    }

    private List<CouponScope> availableCouponScopes(Coupon currentCoupon) {
        LinkedHashSet<CouponScope> scopes = Arrays.stream(CouponScope.values())
                .filter(scope -> scope != CouponScope.CUSTOMER_GROUP)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (currentCoupon != null && currentCoupon.getScope() == CouponScope.CUSTOMER_GROUP) {
            scopes.add(CouponScope.CUSTOMER_GROUP);
        }

        return new ArrayList<>(scopes);
    }

    private String normalizeDelimitedValues(String value) {
        if (value == null) {
            return null;
        }

        LinkedHashSet<String> values = Arrays.stream(value.split("[,;|\\n\\r\\t]+"))
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (values.isEmpty()) {
            return null;
        }
        return String.join(", ", values);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
