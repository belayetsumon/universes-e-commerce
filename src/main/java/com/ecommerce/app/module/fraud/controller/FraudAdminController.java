package com.ecommerce.app.module.fraud.controller;

import com.ecommerce.app.module.fraud.dto.FraudAdminSearchCriteria;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentReviewRequest;
import com.ecommerce.app.module.fraud.dto.FraudBlocklistRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseAssignRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseResolveRequest;
import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.dto.FraudRuleRequest;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudBlockScope;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudCasePriority;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.FraudRuleOperator;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import com.ecommerce.app.module.fraud.security.FraudPermissions;
import com.ecommerce.app.module.fraud.services.FraudAdminViewService;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.fraud.services.FraudReviewService;
import java.security.Principal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fraud")
@PreAuthorize(FraudPermissions.CAN_READ)
public class FraudAdminController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final FraudAdminViewService fraudAdminViewService;
    private final FraudReviewService fraudReviewService;
    private final FraudCaseService fraudCaseService;

    public FraudAdminController(FraudAdminViewService fraudAdminViewService,
            FraudReviewService fraudReviewService,
            FraudCaseService fraudCaseService) {
        this.fraudAdminViewService = fraudAdminViewService;
        this.fraudReviewService = fraudReviewService;
        this.fraudCaseService = fraudCaseService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Fraud Dashboard");
        model.addAttribute("metrics", fraudAdminViewService.dashboardMetrics());
        model.addAttribute("openCases", fraudAdminViewService.openCases().stream().limit(8).toList());
        model.addAttribute("ruleRows", fraudAdminViewService.triggeredSignalRows(8));
        model.addAttribute("suspiciousRows", fraudAdminViewService.suspiciousValueRows(8));
        return "admin/fraud/dashboard";
    }

    @GetMapping("/assessments")
    public String assessments(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Assessments");
        model.addAttribute("assessments", fraudAdminViewService.searchAssessments(filter, pageable(page, "evaluatedAt")));
        addCommonEnums(model);
        return "admin/fraud/assessments";
    }

    @GetMapping("/assessments/{id}")
    public String assessmentDetail(@PathVariable Long id, Model model) {
        var assessment = fraudAdminViewService.getAssessment(id);
        model.addAttribute("pageTitle", "Fraud Assessment #" + id);
        model.addAttribute("assessment", assessment);
        model.addAttribute("signals", fraudAdminViewService.assessmentSignals(id));
        model.addAttribute("ruleExecutions", fraudAdminViewService.assessmentRuleExecutions(id));
        model.addAttribute("reviewHistory", fraudAdminViewService.assessmentReviewHistory(id));
        model.addAttribute("evidenceList", fraudAdminViewService.assessmentEvidence(id));
        model.addAttribute("events", fraudAdminViewService.orderEvents(assessment.getOrderId()));
        model.addAttribute("reviewRequest", new FraudAssessmentReviewRequest());
        addCommonEnums(model);
        return "admin/fraud/assessment-detail";
    }

    @GetMapping("/assessments/{id}/review")
    public String manualReview(@PathVariable Long id, Model model) {
        var assessment = fraudAdminViewService.getAssessment(id);
        model.addAttribute("pageTitle", "Manual Fraud Review");
        model.addAttribute("assessment", assessment);
        model.addAttribute("signals", fraudAdminViewService.assessmentSignals(id));
        model.addAttribute("ruleExecutions", fraudAdminViewService.assessmentRuleExecutions(id));
        model.addAttribute("reviewHistory", fraudAdminViewService.assessmentReviewHistory(id));
        model.addAttribute("reviewRequest", new FraudAssessmentReviewRequest());
        model.addAttribute("blocklistRequest", new FraudBlocklistRequest());
        addCommonEnums(model);
        return "admin/fraud/manual-review";
    }

    @PostMapping("/assessments/{id}/review")
    @PreAuthorize(FraudPermissions.CAN_REVIEW)
    public String submitReview(@PathVariable Long id,
            @RequestParam String action,
            @ModelAttribute FraudAssessmentReviewRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            switch (action == null ? "" : action) {
                case "approve" -> fraudReviewService.approve(id, request);
                case "reject" -> fraudReviewService.reject(id, request);
                case "verify" -> fraudReviewService.requestVerification(id, request);
                case "hold" -> fraudReviewService.hold(id, request);
                default -> throw new IllegalArgumentException("Unsupported review action.");
            }
            redirectAttributes.addFlashAttribute("successMessage", "Fraud review decision saved.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/assessments/" + id;
    }

    @PostMapping("/assessments/{id}/blocklist")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String blockFromReview(@PathVariable Long id,
            @ModelAttribute FraudBlocklistRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.addBlocklist(request, principalName(principal));
            redirectAttributes.addFlashAttribute("successMessage", "Blocklist entry saved.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/assessments/" + id + "/review";
    }

    @GetMapping("/cases")
    public String cases(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Cases");
        model.addAttribute("cases", fraudAdminViewService.searchCases(filter, pageable(page, "openedAt")));
        addCommonEnums(model);
        return "admin/fraud/cases";
    }

    @GetMapping("/cases/{id}")
    public String caseDetail(@PathVariable Long id, Model model) {
        var fraudCase = fraudAdminViewService.getCase(id);
        model.addAttribute("pageTitle", "Fraud Case " + fraudCase.getCaseNumber());
        model.addAttribute("fraudCase", fraudCase);
        model.addAttribute("assignRequest", new FraudCaseAssignRequest());
        model.addAttribute("resolveRequest", new FraudCaseResolveRequest());
        model.addAttribute("events", fraudAdminViewService.orderEvents(fraudCase.getOrderId()));
        addCommonEnums(model);
        return "admin/fraud/case-detail";
    }

    @PostMapping("/cases/{id}/assign")
    @PreAuthorize(FraudPermissions.CAN_REVIEW)
    public String assignCase(@PathVariable Long id,
            @ModelAttribute FraudCaseAssignRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            fraudCaseService.assign(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Fraud case assigned.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/cases/" + id;
    }

    @PostMapping("/cases/{id}/resolve")
    @PreAuthorize(FraudPermissions.CAN_REVIEW)
    public String resolveCase(@PathVariable Long id,
            @ModelAttribute FraudCaseResolveRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            fraudCaseService.resolve(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Fraud case resolved.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/cases/" + id;
    }

    @GetMapping("/rules")
    public String rules(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Rules");
        model.addAttribute("rules", fraudAdminViewService.searchRules(filter, pageable(page, "priority")));
        addCommonEnums(model);
        return "admin/fraud/rules";
    }

    @GetMapping("/rules/new")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String newRule(Model model) {
        model.addAttribute("pageTitle", "New Fraud Rule");
        model.addAttribute("ruleId", null);
        model.addAttribute("ruleRequest", new FraudRuleRequest());
        addCommonEnums(model);
        return "admin/fraud/rule-form";
    }

    @GetMapping("/rules/{id}/edit")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String editRule(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Fraud Rule");
        model.addAttribute("ruleId", id);
        model.addAttribute("ruleRequest", fraudAdminViewService.toRuleRequest(fraudAdminViewService.getRule(id)));
        addCommonEnums(model);
        return "admin/fraud/rule-form";
    }

    @PostMapping("/rules/save")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String saveRule(@RequestParam(required = false) Long id,
            @ModelAttribute FraudRuleRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.saveRule(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Fraud rule saved.");
            return "redirect:/admin/fraud/rules";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
            return id == null ? "redirect:/admin/fraud/rules/new" : "redirect:/admin/fraud/rules/" + id + "/edit";
        }
    }

    @PostMapping("/rules/{id}/status")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String updateRuleStatus(@PathVariable Long id,
            @RequestParam boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.updateRuleStatus(id, active);
            redirectAttributes.addFlashAttribute("successMessage", "Fraud rule status updated.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/rules";
    }

    @GetMapping("/blocklist")
    public String blocklist(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Blocklist");
        model.addAttribute("blocklist", fraudAdminViewService.searchBlocklist(filter, pageable(page, "createdAt")));
        addCommonEnums(model);
        return "admin/fraud/blocklist";
    }

    @GetMapping("/blocklist/new")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String newBlocklist(Model model) {
        model.addAttribute("pageTitle", "New Blocklist Entry");
        model.addAttribute("blocklistRequest", new FraudBlocklistRequest());
        addCommonEnums(model);
        return "admin/fraud/blocklist-form";
    }

    @PostMapping("/blocklist/save")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String saveBlocklist(@ModelAttribute FraudBlocklistRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.addBlocklist(request, principalName(principal));
            redirectAttributes.addFlashAttribute("successMessage", "Blocklist entry saved.");
            return "redirect:/admin/fraud/blocklist";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
            return "redirect:/admin/fraud/blocklist/new";
        }
    }

    @PostMapping("/blocklist/{id}/status")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String updateBlocklistStatus(@PathVariable Long id,
            @RequestParam boolean active,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.updateBlocklistStatus(id, active, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Blocklist status updated.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/blocklist";
    }

    @GetMapping("/configuration")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String configuration(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long editId,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Configuration");
        model.addAttribute("configurations", fraudAdminViewService.searchConfigurations(filter, pageable(page, "configKey")));
        model.addAttribute("editId", editId);
        model.addAttribute("configurationRequest", editId == null
                ? new FraudConfigurationRequest()
                : fraudAdminViewService.toConfigurationRequest(fraudAdminViewService.getConfiguration(editId)));
        return "admin/fraud/configuration";
    }

    @PostMapping("/configuration/save")
    @PreAuthorize(FraudPermissions.CAN_ADMIN)
    public String saveConfiguration(@RequestParam(required = false) Long id,
            @ModelAttribute FraudConfigurationRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            fraudAdminViewService.saveConfiguration(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Fraud configuration saved.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", cleanMessage(ex));
        }
        return "redirect:/admin/fraud/configuration";
    }

    @GetMapping("/reports")
    @PreAuthorize(FraudPermissions.CAN_FINANCE_OR_READ)
    public String reports(@ModelAttribute("filter") FraudAdminSearchCriteria filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("pageTitle", "Fraud Reports");
        model.addAttribute("metrics", fraudAdminViewService.dashboardMetrics());
        model.addAttribute("ruleRows", fraudAdminViewService.triggeredSignalRows(20));
        model.addAttribute("suspiciousRows", fraudAdminViewService.suspiciousValueRows(20));
        model.addAttribute("events", fraudAdminViewService.searchEvents(filter, pageable(page, "eventTime")));
        model.addAttribute("vendorProfiles", fraudAdminViewService.searchVendorProfiles(filter, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "riskScore"))));
        addCommonEnums(model);
        return "admin/fraud/reports";
    }

    private Pageable pageable(int page, String sortField) {
        return PageRequest.of(Math.max(page, 0), DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, sortField));
    }

    private void addCommonEnums(Model model) {
        model.addAttribute("riskLevels", FraudRiskLevel.values());
        model.addAttribute("assessmentStatuses", FraudAssessmentStatus.values());
        model.addAttribute("fraudDecisions", FraudDecision.values());
        model.addAttribute("fraudActions", FraudAction.values());
        model.addAttribute("caseStatuses", FraudCaseStatus.values());
        model.addAttribute("casePriorities", FraudCasePriority.values());
        model.addAttribute("ruleTypes", FraudRuleType.values());
        model.addAttribute("ruleOperators", FraudRuleOperator.values());
        model.addAttribute("blockTypes", FraudBlockType.values());
        model.addAttribute("blockScopes", FraudBlockScope.values());
    }

    private String principalName(Principal principal) {
        return principal == null || principal.getName() == null ? "fraud-admin" : principal.getName();
    }

    private String cleanMessage(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Fraud admin action failed." : ex.getMessage();
    }
}
