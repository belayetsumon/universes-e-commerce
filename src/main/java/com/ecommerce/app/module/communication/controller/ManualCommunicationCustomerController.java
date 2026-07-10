package com.ecommerce.app.module.communication.controller;

import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.dto.ManualMessageRequest;
import com.ecommerce.app.module.communication.dto.ManualMessageResponse;
import com.ecommerce.app.module.communication.model.ManualActorType;
import com.ecommerce.app.module.communication.model.ManualAudience;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.services.ManualCommunicationDispatchService;
import com.ecommerce.app.module.communication.services.ManualCommunicationHistoryService;
import com.ecommerce.app.module.communication.services.ManualCommunicationPermissionService;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/communication/manual")
public class ManualCommunicationCustomerController {

    private final ManualCommunicationPermissionService permissionService;
    private final ManualCommunicationDispatchService dispatchService;
    private final ManualCommunicationHistoryService historyService;

    public ManualCommunicationCustomerController(
            ManualCommunicationPermissionService permissionService,
            ManualCommunicationDispatchService dispatchService,
            ManualCommunicationHistoryService historyService) {
        this.permissionService = permissionService;
        this.dispatchService = dispatchService;
        this.historyService = historyService;
    }

    @ModelAttribute
    public void common(Model model) {
        model.addAttribute("messageChannels", java.util.List.of(MessageChannel.EMAIL, MessageChannel.SMS, MessageChannel.IN_APP, MessageChannel.PUSH));
        model.addAttribute("messageTypes", MessageType.values());
        model.addAttribute("messageStatuses", MessageStatus.values());
        model.addAttribute("manualAudiences", permissionService.allowedAudiences(ManualActorType.CUSTOMER));
        model.addAttribute("basePath", "/customer/communication/manual");
        model.addAttribute("scope", "customer");
    }

    @GetMapping("/compose")
    public String compose(Model model) {
        permissionService.requireCustomerActor();
        ManualMessageRequest request = new ManualMessageRequest();
        request.setAudience(ManualAudience.ADMIN);
        prepareCompose(model, request);
        return "customer/communication/manual-compose";
    }

    @PostMapping("/send")
    public String send(@Valid @ModelAttribute("manualMessage") ManualMessageRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        ManualCommunicationActor actor = permissionService.requireCustomerActor();
        request.setAudience(ManualAudience.ADMIN);
        if (result.hasErrors()) {
            prepareCompose(model, request);
            return "customer/communication/manual-compose";
        }
        try {
            ManualMessageResponse response = dispatchService.send(actor, request);
            redirectAttributes.addFlashAttribute("successMessage", summary(response));
            return "redirect:/customer/communication/manual/sent";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepareCompose(model, request);
            return "customer/communication/manual-compose";
        }
    }

    @GetMapping("/inbox")
    public String inbox(Model model,
            @RequestParam(required = false) MessageChannel channel,
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        ManualCommunicationActor actor = permissionService.requireCustomerActor();
        model.addAttribute("page", historyService.findInbox(actor, channel, status, q, pageable(page, size)));
        historyModel(model, "inbox", channel, status, q);
        return "customer/communication/manual-history";
    }

    @GetMapping("/sent")
    public String sent(Model model,
            @RequestParam(required = false) MessageChannel channel,
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        ManualCommunicationActor actor = permissionService.requireCustomerActor();
        model.addAttribute("page", historyService.findSent(actor, channel, status, q, pageable(page, size)));
        historyModel(model, "sent", channel, status, q);
        return "customer/communication/manual-history";
    }

    private void prepareCompose(Model model, ManualMessageRequest request) {
        if (request.getChannels().isEmpty()) {
            request.setChannels(Set.of(MessageChannel.IN_APP));
        }
        model.addAttribute("manualMessage", request);
    }

    private void historyModel(Model model, String activeView, MessageChannel channel, MessageStatus status, String q) {
        model.addAttribute("activeView", activeView);
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("q", q);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 10), 100), Sort.by(Sort.Order.desc("sentAt"), Sort.Order.desc("id")));
    }

    private String summary(ManualMessageResponse response) {
        return "Manual message processed for " + response.getRecipientCount() + " recipient(s): "
                + response.getSentCount() + " sent, " + response.getQueuedCount() + " queued, "
                + response.getSkippedCount() + " skipped, " + response.getFailedCount() + " failed.";
    }
}
