package com.ecommerce.app.module.communication.controller;

import com.ecommerce.app.module.communication.services.CommunicationNotificationService;
import com.ecommerce.app.module.communication.services.ManualCommunicationPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/communication/messages")
public class CommunicationMessageReportController {

    private final ManualCommunicationPermissionService permissionService;
    private final CommunicationNotificationService notificationService;

    public CommunicationMessageReportController(
            ManualCommunicationPermissionService permissionService,
            CommunicationNotificationService notificationService) {
        this.permissionService = permissionService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String messageHistory(Model model) {
        permissionService.requireAdminActor();
        model.addAttribute("rows", notificationService.findInAppMessageHistory());
        model.addAttribute("pageTitle", "In-App Message History");
        return "admin/communication/message-history";
    }

    @GetMapping("/{messageId}/report")
    public String messageReport(Model model, @PathVariable Long messageId) {
        permissionService.requireAdminActor();
        model.addAttribute("message", notificationService.requireMessage(messageId));
        model.addAttribute("report", notificationService.getReadReport(messageId));
        model.addAttribute("receiverReport", notificationService.getReceiverTypeReport(messageId));
        model.addAttribute("pageTitle", "In-App Message Read Report");
        return "admin/communication/message-report";
    }

    @GetMapping("/{messageId}/recipients")
    public String messageRecipients(Model model, @PathVariable Long messageId) {
        permissionService.requireAdminActor();
        model.addAttribute("message", notificationService.requireMessage(messageId));
        model.addAttribute("recipients", notificationService.findRecipients(messageId));
        model.addAttribute("pageTitle", "In-App Message Recipients");
        return "admin/communication/message-recipients";
    }
}
