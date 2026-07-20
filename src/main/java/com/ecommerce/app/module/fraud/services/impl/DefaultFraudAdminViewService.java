package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudAdminMetric;
import com.ecommerce.app.module.fraud.dto.FraudAdminReportRow;
import com.ecommerce.app.module.fraud.dto.FraudAdminSearchCriteria;
import com.ecommerce.app.module.fraud.dto.FraudBlocklistRequest;
import com.ecommerce.app.module.fraud.dto.FraudConfigurationAdminRow;
import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.dto.FraudRuleRequest;
import com.ecommerce.app.module.fraud.exception.FraudNotFoundException;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEvidence;
import com.ecommerce.app.module.fraud.model.FraudReviewHistory;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleExecution;
import com.ecommerce.app.module.fraud.model.FraudSignal;
import com.ecommerce.app.module.fraud.model.VendorRiskProfile;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.repository.FraudEvidenceRepository;
import com.ecommerce.app.module.fraud.repository.FraudReviewHistoryRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleExecutionRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleRepository;
import com.ecommerce.app.module.fraud.repository.FraudSignalRepository;
import com.ecommerce.app.module.fraud.repository.VendorRiskProfileRepository;
import com.ecommerce.app.module.fraud.security.FraudPrivacySupport;
import com.ecommerce.app.module.fraud.services.FraudAdminViewService;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudAdminViewService implements FraudAdminViewService {

    private static final Set<FraudCaseStatus> OPEN_CASE_STATUSES = Set.of(
            FraudCaseStatus.OPEN,
            FraudCaseStatus.ASSIGNED,
            FraudCaseStatus.IN_REVIEW,
            FraudCaseStatus.ESCALATED
    );

    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final FraudRuleRepository fraudRuleRepository;
    private final FraudBlocklistRepository fraudBlocklistRepository;
    private final FraudConfigurationRepository fraudConfigurationRepository;
    private final FraudSignalRepository fraudSignalRepository;
    private final FraudRuleExecutionRepository fraudRuleExecutionRepository;
    private final FraudReviewHistoryRepository fraudReviewHistoryRepository;
    private final FraudEvidenceRepository fraudEvidenceRepository;
    private final FraudEventLogRepository fraudEventLogRepository;
    private final VendorRiskProfileRepository vendorRiskProfileRepository;
    private final FraudAuditService fraudAuditService;

    public DefaultFraudAdminViewService(FraudAssessmentRepository fraudAssessmentRepository,
            FraudCaseRepository fraudCaseRepository,
            FraudRuleRepository fraudRuleRepository,
            FraudBlocklistRepository fraudBlocklistRepository,
            FraudConfigurationRepository fraudConfigurationRepository,
            FraudSignalRepository fraudSignalRepository,
            FraudRuleExecutionRepository fraudRuleExecutionRepository,
            FraudReviewHistoryRepository fraudReviewHistoryRepository,
            FraudEvidenceRepository fraudEvidenceRepository,
            FraudEventLogRepository fraudEventLogRepository,
            VendorRiskProfileRepository vendorRiskProfileRepository,
            FraudAuditService fraudAuditService) {
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.fraudRuleRepository = fraudRuleRepository;
        this.fraudBlocklistRepository = fraudBlocklistRepository;
        this.fraudConfigurationRepository = fraudConfigurationRepository;
        this.fraudSignalRepository = fraudSignalRepository;
        this.fraudRuleExecutionRepository = fraudRuleExecutionRepository;
        this.fraudReviewHistoryRepository = fraudReviewHistoryRepository;
        this.fraudEvidenceRepository = fraudEvidenceRepository;
        this.fraudEventLogRepository = fraudEventLogRepository;
        this.vendorRiskProfileRepository = vendorRiskProfileRepository;
        this.fraudAuditService = fraudAuditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudAdminMetric> dashboardMetrics() {
        return List.of(
                new FraudAdminMetric("Total assessed orders", fraudAssessmentRepository.count(), "primary", "bi-shield-check"),
                new FraudAdminMetric("Low risk", fraudAssessmentRepository.countByRiskLevel(FraudRiskLevel.LOW), "success", "bi-check2-circle"),
                new FraudAdminMetric("Medium risk", fraudAssessmentRepository.countByRiskLevel(FraudRiskLevel.MEDIUM), "warning", "bi-exclamation-circle"),
                new FraudAdminMetric("High risk", fraudAssessmentRepository.countByRiskLevel(FraudRiskLevel.HIGH), "danger", "bi-exclamation-triangle"),
                new FraudAdminMetric("Critical risk", fraudAssessmentRepository.countByRiskLevel(FraudRiskLevel.CRITICAL), "dark", "bi-shield-fill-exclamation"),
                new FraudAdminMetric("Manual review", fraudAssessmentRepository.countByStatus(FraudAssessmentStatus.MANUAL_REVIEW), "warning", "bi-person-lines-fill"),
                new FraudAdminMetric("Blocked", fraudAssessmentRepository.countByDecision(FraudDecision.BLOCK), "danger", "bi-ban"),
                new FraudAdminMetric("Rejected", fraudAssessmentRepository.countByDecision(FraudDecision.REJECT), "danger", "bi-x-octagon"),
                new FraudAdminMetric("Open cases", OPEN_CASE_STATUSES.stream().mapToLong(fraudCaseRepository::countByCaseStatus).sum(), "info", "bi-folder2-open"),
                new FraudAdminMetric("Active rules", fraudRuleRepository.countByActive(true), "primary", "bi-sliders"),
                new FraudAdminMetric("Active blocklist", fraudBlocklistRepository.countByActive(true), "dark", "bi-shield-lock"),
                new FraudAdminMetric("Vendor payout holds", vendorRiskProfileRepository.count((root, query, cb) -> cb.isTrue(root.get("payoutHeld"))), "danger", "bi-bank")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudAdminReportRow> triggeredSignalRows(int limit) {
        return fraudSignalRepository.countTriggeredBySignalCode().stream()
                .limit(limit)
                .map(row -> new FraudAdminReportRow(String.valueOf(row[0]), "", asLong(row[1])))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudAdminReportRow> suspiciousValueRows(int limit) {
        return fraudSignalRepository.countTriggeredValues().stream()
                .limit(limit)
                .map(row -> new FraudAdminReportRow(String.valueOf(row[0]), FraudPrivacySupport.maskIdentifier(String.valueOf(row[1])), asLong(row[2])))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAssessment> searchAssessments(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudAssessmentRepository.findAll(assessmentSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudCase> searchCases(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudCaseRepository.findAll(caseSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudRule> searchRules(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudRuleRepository.findAll(ruleSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudBlocklist> searchBlocklist(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudBlocklistRepository.findAll(blocklistSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudConfigurationAdminRow> searchConfigurations(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudConfigurationRepository.findAll(configurationSpec(criteria), pageable)
                .map(this::toConfigurationAdminRow);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudEventLog> searchEvents(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return fraudEventLogRepository.findAll(eventSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorRiskProfile> searchVendorProfiles(FraudAdminSearchCriteria criteria, Pageable pageable) {
        return vendorRiskProfileRepository.findAll(vendorProfileSpec(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAssessment getAssessment(Long id) {
        return fraudAssessmentRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud assessment not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public FraudCase getCase(Long id) {
        return fraudCaseRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud case not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public FraudRule getRule(Long id) {
        return fraudRuleRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud rule not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public FraudConfiguration getConfiguration(Long id) {
        return fraudConfigurationRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud configuration not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudSignal> assessmentSignals(Long assessmentId) {
        return fraudSignalRepository.findByAssessment_IdOrderByIdAsc(assessmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudRuleExecution> assessmentRuleExecutions(Long assessmentId) {
        return fraudRuleExecutionRepository.findByAssessment_IdOrderByIdAsc(assessmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudReviewHistory> assessmentReviewHistory(Long assessmentId) {
        return fraudReviewHistoryRepository.findByAssessment_IdOrderByReviewedAtDesc(assessmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudEvidence> assessmentEvidence(Long assessmentId) {
        return fraudEvidenceRepository.findByAssessment_IdOrderByIdDesc(assessmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudEventLog> orderEvents(Long orderId) {
        return orderId == null ? List.of() : fraudEventLogRepository.findByOrderIdOrderByEventTimeDesc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudCase> openCases() {
        return fraudCaseRepository.findByCaseStatusInOrderByIdDesc(OPEN_CASE_STATUSES);
    }

    @Override
    public FraudRuleRequest toRuleRequest(FraudRule rule) {
        FraudRuleRequest request = new FraudRuleRequest();
        if (rule == null) {
            return request;
        }
        request.setRuleCode(rule.getRuleCode());
        request.setRuleName(rule.getRuleName());
        request.setDescription(rule.getDescription());
        request.setRuleType(rule.getRuleType());
        request.setSignalCode(rule.getSignalCode());
        request.setOperator(rule.getOperator());
        request.setComparisonValue(rule.getComparisonValue());
        request.setScoreImpact(rule.getScoreImpact());
        request.setPriority(rule.getPriority());
        request.setAction(rule.getAction());
        request.setHardBlock(rule.isHardBlock());
        request.setActive(rule.isActive());
        request.setEffectiveStartAt(rule.getEffectiveStartAt());
        request.setEffectiveEndAt(rule.getEffectiveEndAt());
        request.setVendorId(rule.getVendorId());
        request.setProductId(rule.getProductId());
        request.setCategoryId(rule.getCategoryId());
        request.setPaymentMethod(rule.getPaymentMethod());
        request.setCountry(rule.getCountry());
        request.setDistrict(rule.getDistrict());
        request.setChannel(rule.getChannel());
        request.setRuleConfigurationJson(rule.getRuleConfigurationJson());
        return request;
    }

    @Override
    @Transactional
    public FraudRule saveRule(Long id, FraudRuleRequest request) {
        validateRuleRequest(id, request);
        FraudRule rule = id == null ? new FraudRule() : getRule(id);
        rule.setRuleCode(clean(request.getRuleCode()));
        rule.setRuleName(clean(request.getRuleName()));
        rule.setDescription(clean(request.getDescription()));
        rule.setRuleType(request.getRuleType());
        rule.setSignalCode(clean(request.getSignalCode()));
        rule.setOperator(request.getOperator());
        rule.setComparisonValue(clean(request.getComparisonValue()));
        rule.setScoreImpact(request.getScoreImpact());
        rule.setPriority(request.getPriority());
        rule.setAction(request.getAction());
        rule.setHardBlock(request.isHardBlock());
        rule.setActive(request.isActive());
        rule.setEffectiveStartAt(request.getEffectiveStartAt());
        rule.setEffectiveEndAt(request.getEffectiveEndAt());
        rule.setVendorId(request.getVendorId());
        rule.setProductId(request.getProductId());
        rule.setCategoryId(request.getCategoryId());
        rule.setPaymentMethod(clean(request.getPaymentMethod()));
        rule.setCountry(clean(request.getCountry()));
        rule.setDistrict(clean(request.getDistrict()));
        rule.setChannel(clean(request.getChannel()));
        rule.setRuleConfigurationJson(clean(request.getRuleConfigurationJson()));
        FraudRule saved = fraudRuleRepository.save(rule);
        fraudAuditService.record("FRAUD_RULE", saved.getId(), FraudAction.MANUAL_REVIEW, "Fraud rule saved.", "{}");
        return saved;
    }

    @Override
    @Transactional
    public void updateRuleStatus(Long id, boolean active) {
        FraudRule rule = getRule(id);
        rule.setActive(active);
        fraudRuleRepository.save(rule);
        fraudAuditService.record("FRAUD_RULE", id, active ? FraudAction.ALLOW : FraudAction.HOLD, "Fraud rule status changed.", "{}");
    }

    @Override
    @Transactional
    public FraudBlocklist addBlocklist(FraudBlocklistRequest request, String createdBy) {
        if (request == null || request.getBlockType() == null || clean(request.getBlockValue()) == null || clean(request.getReason()) == null) {
            throw new FraudValidationException("Block type, value, and reason are required.");
        }
        String hashedValue = FraudHashingSupport.sha256(request.getBlockValue());
        FraudBlocklist entry = fraudBlocklistRepository
                .findByBlockTypeAndHashedValueAndActiveTrue(request.getBlockType(), hashedValue)
                .orElseGet(FraudBlocklist::new);
        entry.setBlockType(request.getBlockType());
        entry.setHashedValue(hashedValue);
        entry.setMaskedValue(FraudPrivacySupport.maskIdentifier(request.getBlockValue()));
        entry.setReason(clean(request.getReason()));
        entry.setScope(request.getScope());
        entry.setTemporary(request.isTemporary());
        entry.setExpiresAt(request.getExpiresAt());
        entry.setCreatedByUser(clean(createdBy));
        entry.setActive(true);
        FraudBlocklist saved = fraudBlocklistRepository.save(entry);
        fraudAuditService.record("FRAUD_BLOCKLIST", saved.getId(), FraudAction.BLOCK, request.getReason(),
                "{\"blockType\":\"" + json(String.valueOf(request.getBlockType()))
                        + "\",\"maskedValue\":\"" + json(entry.getMaskedValue()) + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public void updateBlocklistStatus(Long id, boolean active, String reason) {
        FraudBlocklist blocklist = fraudBlocklistRepository.findById(id)
                .orElseThrow(() -> new FraudNotFoundException("Fraud blocklist entry not found."));
        blocklist.setActive(active);
        fraudBlocklistRepository.save(blocklist);
        fraudAuditService.record("FRAUD_BLOCKLIST", id, active ? FraudAction.BLOCK : FraudAction.ALLOW,
                clean(reason) == null ? "Fraud blocklist status changed." : reason, "{}");
    }

    @Override
    public FraudConfigurationRequest toConfigurationRequest(FraudConfiguration configuration) {
        FraudConfigurationRequest request = new FraudConfigurationRequest();
        if (configuration == null) {
            return request;
        }
        request.setConfigKey(configuration.getConfigKey());
        request.setConfigValue(configuration.getConfigValue());
        request.setDescription(configuration.getDescription());
        request.setActive(configuration.isActive());
        return request;
    }

    @Override
    @Transactional
    public FraudConfiguration saveConfiguration(Long id, FraudConfigurationRequest request) {
        if (request == null || clean(request.getConfigKey()) == null || clean(request.getConfigValue()) == null) {
            throw new FraudValidationException("Configuration key and value are required.");
        }
        FraudConfiguration configuration = id == null
                ? fraudConfigurationRepository.findByConfigKey(clean(request.getConfigKey())).orElseGet(FraudConfiguration::new)
                : fraudConfigurationRepository.findById(id).orElseThrow(() -> new FraudNotFoundException("Fraud configuration not found."));
        configuration.setConfigKey(clean(request.getConfigKey()));
        configuration.setConfigValue(request.getConfigValue().trim());
        configuration.setDescription(clean(request.getDescription()));
        configuration.setActive(request.isActive());
        FraudConfiguration saved = fraudConfigurationRepository.save(configuration);
        fraudAuditService.record("FRAUD_CONFIGURATION", saved.getId(), FraudAction.MANUAL_REVIEW,
                "Fraud configuration saved.",
                "{\"configKey\":\"" + json(saved.getConfigKey())
                        + "\",\"sensitive\":" + isSensitiveConfig(saved.getConfigKey()) + "}");
        return saved;
    }

    private FraudConfigurationAdminRow toConfigurationAdminRow(FraudConfiguration configuration) {
        FraudConfigurationAdminRow row = new FraudConfigurationAdminRow();
        row.setId(configuration.getId());
        row.setConfigKey(configuration.getConfigKey());
        row.setDescription(configuration.getDescription());
        row.setActive(configuration.isActive());
        row.setSensitive(isSensitiveConfig(configuration.getConfigKey()));
        row.setDisplayValue(row.isSensitive() ? "********" : configuration.getConfigValue());
        return row;
    }

    private Specification<FraudAssessment> assessmentSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("orderId"), filter.getOrderId());
            equal(predicates, cb, root.get("customerId"), filter.getCustomerId());
            equal(predicates, cb, root.get("vendorId"), filter.getVendorId());
            equal(predicates, cb, root.get("riskLevel"), filter.getRiskLevel());
            equal(predicates, cb, root.get("decision"), filter.getDecision());
            equal(predicates, cb, root.get("status"), filter.getAssessmentStatus());
            dateRange(predicates, cb, root.get("evaluatedAt"), filter.getFromDate(), filter.getToDate());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderUuid")), q),
                        cb.like(cb.lower(root.get("correlationId")), q),
                        cb.like(cb.lower(root.get("decisionReason")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FraudCase> caseSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("orderId"), filter.getOrderId());
            equal(predicates, cb, root.get("customerId"), filter.getCustomerId());
            equal(predicates, cb, root.get("vendorId"), filter.getVendorId());
            equal(predicates, cb, root.get("caseStatus"), filter.getCaseStatus());
            dateRange(predicates, cb, root.get("openedAt"), filter.getFromDate(), filter.getToDate());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("caseNumber")), q),
                        cb.like(cb.lower(root.get("caseReason")), q),
                        cb.like(cb.lower(root.get("assignedInvestigator")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FraudRule> ruleSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("ruleType"), filter.getRuleType());
            equal(predicates, cb, root.get("active"), filter.getActive());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ruleCode")), q),
                        cb.like(cb.lower(root.get("ruleName")), q),
                        cb.like(cb.lower(root.get("signalCode")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FraudBlocklist> blocklistSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("blockType"), filter.getBlockType());
            equal(predicates, cb, root.get("active"), filter.getActive());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("maskedValue")), q),
                        cb.like(cb.lower(root.get("reason")), q),
                        cb.like(cb.lower(root.get("createdByUser")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FraudConfiguration> configurationSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("active"), filter.getActive());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("configKey")), q),
                        cb.like(cb.lower(root.get("description")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FraudEventLog> eventSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("orderId"), filter.getOrderId());
            equal(predicates, cb, root.get("customerId"), filter.getCustomerId());
            equal(predicates, cb, root.get("vendorId"), filter.getVendorId());
            dateRange(predicates, cb, root.get("eventTime"), filter.getFromDate(), filter.getToDate());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("aggregateType")), q),
                        cb.like(cb.lower(root.get("correlationId")), q),
                        cb.like(cb.lower(root.get("payloadJson")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<VendorRiskProfile> vendorProfileSpec(FraudAdminSearchCriteria criteria) {
        FraudAdminSearchCriteria filter = safeCriteria(criteria);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, cb, root.get("vendorId"), filter.getVendorId());
            equal(predicates, cb, root.get("riskLevel"), filter.getRiskLevel());
            String q = normalizedLike(filter.getQ());
            if (q != null) {
                predicates.add(cb.like(cb.lower(root.get("lastRiskReason")), q));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateRuleRequest(Long id, FraudRuleRequest request) {
        if (request == null || clean(request.getRuleCode()) == null || clean(request.getRuleName()) == null || request.getRuleType() == null) {
            throw new FraudValidationException("Rule code, name, and type are required.");
        }
        fraudRuleRepository.findByRuleCode(clean(request.getRuleCode()))
                .filter(existing -> id == null || !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new FraudValidationException("Rule code already exists.");
                });
    }

    private FraudAdminSearchCriteria safeCriteria(FraudAdminSearchCriteria criteria) {
        return criteria == null ? new FraudAdminSearchCriteria() : criteria;
    }

    private void equal(List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<?> path, Object value) {
        if (value != null) {
            predicates.add(cb.equal(path, value));
        }
    }

    private void dateRange(List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<LocalDateTime> path, LocalDate from, LocalDate to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, from.atStartOfDay()));
        }
        if (to != null) {
            predicates.add(cb.lessThan(path, to.plusDays(1).atStartOfDay()));
        }
    }

    private String normalizedLike(String value) {
        String clean = clean(value);
        return clean == null ? null : "%" + clean.toLowerCase(Locale.ROOT) + "%";
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    private boolean isSensitiveConfig(String key) {
        String cleaned = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return cleaned.contains("secret")
                || cleaned.contains("token")
                || cleaned.contains("password")
                || cleaned.contains("key");
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
