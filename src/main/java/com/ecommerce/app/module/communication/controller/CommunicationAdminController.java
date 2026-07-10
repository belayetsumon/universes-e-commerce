package com.ecommerce.app.module.communication.controller;

import com.ecommerce.app.module.communication.model.CommunicationSetting;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageRoutingRule;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.MessageTemplate;
import com.ecommerce.app.module.communication.model.ProviderType;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import com.ecommerce.app.module.communication.repository.MessageProviderRepository;
import com.ecommerce.app.module.communication.repository.MessageRoutingRuleRepository;
import com.ecommerce.app.module.communication.repository.MessageTemplateRepository;
import com.ecommerce.app.module.communication.services.CommunicationSettingsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/communication")
public class CommunicationAdminController {

    private final MessageTemplateRepository templateRepository;
    private final MessageProviderRepository providerRepository;
    private final MessageRoutingRuleRepository routingRuleRepository;
    private final MessageJobRepository jobRepository;
    private final MessageLogRepository logRepository;
    private final CommunicationSettingsService settingsService;

    public CommunicationAdminController(
            MessageTemplateRepository templateRepository,
            MessageProviderRepository providerRepository,
            MessageRoutingRuleRepository routingRuleRepository,
            MessageJobRepository jobRepository,
            MessageLogRepository logRepository,
            CommunicationSettingsService settingsService) {
        this.templateRepository = templateRepository;
        this.providerRepository = providerRepository;
        this.routingRuleRepository = routingRuleRepository;
        this.jobRepository = jobRepository;
        this.logRepository = logRepository;
        this.settingsService = settingsService;
    }

    @ModelAttribute
    public void loadCommunicationDropdowns(Model model) {
        model.addAttribute("messageChannels", MessageChannel.values());
        model.addAttribute("messageEventTypes", MessageEventType.values());
        model.addAttribute("deliveryModes", DeliveryMode.values());
        model.addAttribute("messageStatuses", MessageStatus.values());
        model.addAttribute("providerTypes", ProviderType.values());
        model.addAttribute("activeProviders", providerRepository.findAll(Sort.by(Sort.Order.asc("channel"), Sort.Order.asc("priority"), Sort.Order.asc("providerName"))));
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/communication/templates";
    }

    @GetMapping("/templates")
    public String templates(Model model) {
        model.addAttribute("templates", templateRepository.findAll(Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))));
        return "admin/communication/templates";
    }

    @GetMapping("/templates/new")
    public String createTemplate(Model model) {
        MessageTemplate template = new MessageTemplate();
        template.setVariables("{{customerName}}, {{orderNumber}}, {{orderTotal}}, {{trackingNumber}}");
        model.addAttribute("template", template);
        return "admin/communication/template-form";
    }

    @GetMapping("/templates/{id}/edit")
    public String editTemplate(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        MessageTemplate template = templateRepository.findById(id).orElse(null);
        if (template == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message template was not found.");
            return "redirect:/admin/communication/templates";
        }
        model.addAttribute("template", template);
        return "admin/communication/template-form";
    }

    @PostMapping("/templates/save")
    public String saveTemplate(@Valid @ModelAttribute("template") MessageTemplate template, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/communication/template-form";
        }
        templateRepository.save(template);
        redirectAttributes.addFlashAttribute("successMessage", "Message template saved successfully.");
        return "redirect:/admin/communication/templates";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        templateRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Message template deleted successfully.");
        return "redirect:/admin/communication/templates";
    }

    @GetMapping("/providers")
    public String providers(Model model) {
        model.addAttribute("providers", providerRepository.findAll(Sort.by(Sort.Order.asc("channel"), Sort.Order.asc("priority"), Sort.Order.asc("providerName"))));
        return "admin/communication/providers";
    }

    @GetMapping("/providers/new")
    public String createProvider(Model model) {
        model.addAttribute("provider", new MessageProvider());
        return "admin/communication/provider-form";
    }

    @GetMapping("/providers/{id}/edit")
    public String editProvider(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        MessageProvider provider = providerRepository.findById(id).orElse(null);
        if (provider == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message provider was not found.");
            return "redirect:/admin/communication/providers";
        }
        model.addAttribute("provider", provider);
        return "admin/communication/provider-form";
    }

    @PostMapping("/providers/save")
    public String saveProvider(@Valid @ModelAttribute("provider") MessageProvider provider, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/communication/provider-form";
        }
        providerRepository.save(provider);
        redirectAttributes.addFlashAttribute("successMessage", "Message provider saved successfully.");
        return "redirect:/admin/communication/providers";
    }

    @PostMapping("/providers/{id}/delete")
    public String deleteProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        providerRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Message provider deleted successfully.");
        return "redirect:/admin/communication/providers";
    }

    @GetMapping("/routing-rules")
    public String routingRules(Model model) {
        model.addAttribute("routingRules", routingRuleRepository.findAll(Sort.by(Sort.Order.asc("eventType"), Sort.Order.asc("channel"), Sort.Order.desc("minVolume"))));
        return "admin/communication/routing-rules";
    }

    @GetMapping("/routing-rules/new")
    public String createRoutingRule(Model model) {
        model.addAttribute("routingRule", new MessageRoutingRule());
        return "admin/communication/routing-rule-form";
    }

    @GetMapping("/routing-rules/{id}/edit")
    public String editRoutingRule(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        MessageRoutingRule routingRule = routingRuleRepository.findById(id).orElse(null);
        if (routingRule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Routing rule was not found.");
            return "redirect:/admin/communication/routing-rules";
        }
        model.addAttribute("routingRule", routingRule);
        return "admin/communication/routing-rule-form";
    }

    @PostMapping("/routing-rules/save")
    public String saveRoutingRule(
            @Valid @ModelAttribute("routingRule") MessageRoutingRule routingRule,
            BindingResult result,
            @RequestParam(value = "providerId", required = false) Long providerId,
            RedirectAttributes redirectAttributes) {
        if (providerId != null) {
            providerRepository.findById(providerId).ifPresent(routingRule::setProvider);
        } else {
            routingRule.setProvider(null);
        }
        if (result.hasErrors()) {
            return "admin/communication/routing-rule-form";
        }
        routingRuleRepository.save(routingRule);
        redirectAttributes.addFlashAttribute("successMessage", "Routing rule saved successfully.");
        return "redirect:/admin/communication/routing-rules";
    }

    @PostMapping("/routing-rules/{id}/delete")
    public String deleteRoutingRule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        routingRuleRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Routing rule deleted successfully.");
        return "redirect:/admin/communication/routing-rules";
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        model.addAttribute("jobs", jobRepository.findAll(PageRequest.of(0, 100, Sort.by(Sort.Order.desc("scheduledAt"), Sort.Order.desc("id")))).getContent());
        return "admin/communication/jobs";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", logRepository.findAll(PageRequest.of(0, 100, Sort.by(Sort.Order.desc("sentAt"), Sort.Order.desc("id")))).getContent());
        return "admin/communication/logs";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "admin/communication/settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@Valid @ModelAttribute("settings") CommunicationSetting settings, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/communication/settings";
        }
        settingsService.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Communication settings saved successfully.");
        return "redirect:/admin/communication/settings";
    }
}
