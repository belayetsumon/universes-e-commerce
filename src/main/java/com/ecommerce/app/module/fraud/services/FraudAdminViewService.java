package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudAdminMetric;
import com.ecommerce.app.module.fraud.dto.FraudAdminReportRow;
import com.ecommerce.app.module.fraud.dto.FraudAdminSearchCriteria;
import com.ecommerce.app.module.fraud.dto.FraudBlocklistRequest;
import com.ecommerce.app.module.fraud.dto.FraudConfigurationAdminRow;
import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.dto.FraudRuleRequest;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import com.ecommerce.app.module.fraud.model.FraudCase;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEvidence;
import com.ecommerce.app.module.fraud.model.FraudReviewHistory;
import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleExecution;
import com.ecommerce.app.module.fraud.model.FraudSignal;
import com.ecommerce.app.module.fraud.model.VendorRiskProfile;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FraudAdminViewService {

    List<FraudAdminMetric> dashboardMetrics();

    List<FraudAdminReportRow> triggeredSignalRows(int limit);

    List<FraudAdminReportRow> suspiciousValueRows(int limit);

    Page<FraudAssessment> searchAssessments(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<FraudCase> searchCases(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<FraudRule> searchRules(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<FraudBlocklist> searchBlocklist(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<FraudConfigurationAdminRow> searchConfigurations(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<FraudEventLog> searchEvents(FraudAdminSearchCriteria criteria, Pageable pageable);

    Page<VendorRiskProfile> searchVendorProfiles(FraudAdminSearchCriteria criteria, Pageable pageable);

    FraudAssessment getAssessment(Long id);

    FraudCase getCase(Long id);

    FraudRule getRule(Long id);

    FraudConfiguration getConfiguration(Long id);

    List<FraudSignal> assessmentSignals(Long assessmentId);

    List<FraudRuleExecution> assessmentRuleExecutions(Long assessmentId);

    List<FraudReviewHistory> assessmentReviewHistory(Long assessmentId);

    List<FraudEvidence> assessmentEvidence(Long assessmentId);

    List<FraudEventLog> orderEvents(Long orderId);

    List<FraudCase> openCases();

    FraudRuleRequest toRuleRequest(FraudRule rule);

    FraudRule saveRule(Long id, FraudRuleRequest request);

    void updateRuleStatus(Long id, boolean active);

    FraudBlocklist addBlocklist(FraudBlocklistRequest request, String createdBy);

    void updateBlocklistStatus(Long id, boolean active, String reason);

    FraudConfigurationRequest toConfigurationRequest(FraudConfiguration configuration);

    FraudConfiguration saveConfiguration(Long id, FraudConfigurationRequest request);
}
