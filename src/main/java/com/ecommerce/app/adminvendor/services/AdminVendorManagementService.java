package com.ecommerce.app.adminvendor.services;

import com.ecommerce.app.adminvendor.dto.AdminVendorFilter;
import com.ecommerce.app.adminvendor.dto.AdminVendorProfileForm;
import com.ecommerce.app.vendor.model.VendorVerifications;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorVerificationsRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminVendorManagementService {

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorVerificationsRepository vendorVerificationsRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Vendorprofile> findAll() {
        try {
            return vendorprofileRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to load vendor profiles.", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Vendorprofile> search(AdminVendorFilter filter) {
        try {
            if (filter == null || !filter.hasActiveFilters()) {
                return findAll();
            }

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Vendorprofile> cq = cb.createQuery(Vendorprofile.class);
            Root<Vendorprofile> root = cq.from(Vendorprofile.class);

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getQ() != null && !filter.getQ().trim().isEmpty()) {
                String keyword = "%" + filter.getQ().trim().toLowerCase(Locale.ROOT) + "%";

                predicates.add(cb.or(
                        containsIgnoreCase(cb, root, "uuid", keyword),
                        containsIgnoreCase(cb, root, "vendorCode", keyword),
                        containsIgnoreCase(cb, root, "companyName", keyword),
                        containsIgnoreCase(cb, root, "firstName", keyword),
                        containsIgnoreCase(cb, root, "lastName", keyword),
                        containsIgnoreCase(cb, root, "phone", keyword),
                        containsIgnoreCase(cb, root, "email", keyword)
                ));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("vendorStatusEnum"), filter.getStatus()));
            }

            if (filter.getCreatedFrom() != null) {
                LocalDateTime from = filter.getCreatedFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("created"), from));
            }

            if (filter.getCreatedTo() != null) {
                LocalDateTime toExclusive = filter.getCreatedTo().plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.<LocalDateTime>get("created"), toExclusive));
            }

            addVerificationPredicate(filter.getEmailVerified(), true, cb, cq, root, predicates);
            addVerificationPredicate(filter.getMobileVerified(), false, cb, cq, root, predicates);

            cq.select(root);
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
            cq.orderBy(cb.desc(root.get("id")));

            return entityManager.createQuery(cq).getResultList();

        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to search vendor profiles.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Vendorprofile> findByUuid(String uuid) {
        try {
            if (uuid == null || uuid.trim().isEmpty()) {
                return Optional.empty();
            }

            return vendorprofileRepository.findByUuid(uuid.trim());

        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to find vendor profile by UUID: " + uuid, ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AdminVendorProfileForm> getEditForm(String uuid) {
        try {
            return findByUuid(uuid).map(AdminVendorProfileForm::from);
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to load vendor edit form for UUID: " + uuid, ex);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> emailVerifiedByVendorUuid() {
        try {
            return verificationFlagByVendorUuid(true);
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to load vendor email verification map.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> mobileVerifiedByVendorUuid() {
        try {
            return verificationFlagByVendorUuid(false);
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to load vendor mobile verification map.", ex);
        }
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerified(String vendorUuid) {
        try {
            if (vendorUuid == null || vendorUuid.trim().isEmpty()) {
                return false;
            }

            return vendorVerificationsRepository.findByVendorprofile_Uuid(vendorUuid.trim())
                    .map(VendorVerifications::isEmailVerified)
                    .orElse(false);

        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException(
                    "Failed to check vendor email verification for UUID: " + vendorUuid,
                    ex
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isMobileVerified(String vendorUuid) {
        try {
            if (vendorUuid == null || vendorUuid.trim().isEmpty()) {
                return false;
            }

            return vendorVerificationsRepository.findByVendorprofile_Uuid(vendorUuid.trim())
                    .map(VendorVerifications::isMobileVerified)
                    .orElse(false);

        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException(
                    "Failed to check vendor mobile verification for UUID: " + vendorUuid,
                    ex
            );
        }
    }

    @Transactional
    public Vendorprofile updateVendor(AdminVendorProfileForm form) {
        try {
            if (form == null) {
                throw new IllegalArgumentException("Vendor profile form must not be null.");
            }

            if (form.getUuid() == null || form.getUuid().trim().isEmpty()) {
                throw new IllegalArgumentException("Vendor profile UUID is required.");
            }

            Vendorprofile vendorprofile = vendorprofileRepository.findByUuid(form.getUuid().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                    "Vendor profile not found. UUID: " + form.getUuid()
            ));
            vendorprofile.setId(vendorprofile.getId());
            vendorprofile.setCompanyName(trim(form.getCompanyName()));
            vendorprofile.setFirstName(trim(form.getFirstName()));
            vendorprofile.setLastName(trim(form.getLastName()));
            vendorprofile.setDesignation(trim(form.getDesignation()));
            vendorprofile.setPhone(trim(form.getPhone()));
            vendorprofile.setEmail(trim(form.getEmail()));
            vendorprofile.setAddress(trim(form.getAddress()));
            vendorprofile.setDescription(trim(form.getDescription()));
            vendorprofile.setVendorStatusEnum(form.getVendorStatusEnum());

            return vendorprofileRepository.save(vendorprofile);

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to update vendor profile.", ex);
        }
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        try {
            if (uuid == null || uuid.trim().isEmpty()) {
                throw new IllegalArgumentException("Vendor profile UUID is required.");
            }

            Vendorprofile vendorprofile = vendorprofileRepository.findByUuid(uuid.trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                    "Vendor profile not found. UUID: " + uuid
            ));

            vendorprofileRepository.delete(vendorprofile);

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AdminVendorManagementException("Failed to delete vendor profile. UUID: " + uuid, ex);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Predicate containsIgnoreCase(
            CriteriaBuilder cb,
            Root<Vendorprofile> root,
            String fieldName,
            String keyword
    ) {
        Expression<String> field = cb.lower(cb.coalesce(root.<String>get(fieldName), ""));
        return cb.like(field, keyword);
    }

    private void addVerificationPredicate(
            Boolean requestedState,
            boolean emailFlag,
            CriteriaBuilder cb,
            CriteriaQuery<Vendorprofile> cq,
            Root<Vendorprofile> root,
            List<Predicate> predicates
    ) {
        if (requestedState == null) {
            return;
        }

        Predicate hasVerifiedRecord = cb.exists(verifiedContactSubquery(emailFlag, cb, cq, root));
        predicates.add(Boolean.TRUE.equals(requestedState) ? hasVerifiedRecord : cb.not(hasVerifiedRecord));
    }

    private Subquery<Long> verifiedContactSubquery(
            boolean emailFlag,
            CriteriaBuilder cb,
            CriteriaQuery<Vendorprofile> cq,
            Root<Vendorprofile> root
    ) {
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<VendorVerifications> verification = subquery.from(VendorVerifications.class);

        Predicate sameVendor = cb.equal(
                verification.get("vendorprofile").get("uuid"),
                root.get("uuid")
        );

        Predicate verified = emailFlag
                ? cb.isTrue(verification.<Boolean>get("emailVerified"))
                : cb.isTrue(verification.<Boolean>get("mobileVerified"));

        subquery.select(verification.<Long>get("id"));
        subquery.where(cb.and(sameVendor, verified));

        return subquery;
    }

    private Map<String, Boolean> verificationFlagByVendorUuid(boolean emailFlag) {
        Map<String, Boolean> verificationMap = new HashMap<>();

        for (VendorVerifications verification : vendorVerificationsRepository.findAll()) {
            Vendorprofile vendorprofile = verification.getVendorprofile();

            if (vendorprofile != null
                    && vendorprofile.getUuid() != null
                    && !vendorprofile.getUuid().trim().isEmpty()) {

                verificationMap.put(
                        vendorprofile.getUuid(),
                        emailFlag ? verification.isEmailVerified() : verification.isMobileVerified()
                );
            }
        }

        return verificationMap;
    }
}
