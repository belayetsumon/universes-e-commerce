package com.ecommerce.app.module.communication.controller;

import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.model.CommunicationRecipient;
import com.ecommerce.app.module.communication.model.ReceiverType;
import com.ecommerce.app.module.communication.services.CommunicationNotificationService;
import com.ecommerce.app.module.communication.services.ManualCommunicationPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommunicationNotificationController {

    private final ManualCommunicationPermissionService permissionService;
    private final CommunicationNotificationService notificationService;

    public CommunicationNotificationController(
            ManualCommunicationPermissionService permissionService,
            CommunicationNotificationService notificationService) {
        this.permissionService = permissionService;
        this.notificationService = notificationService;
    }

    @GetMapping("/customer/notifications")
    public String customerNotifications(Model model, @RequestParam(defaultValue = "all") String view) {
        ManualCommunicationActor actor = permissionService.requireCustomerActor();
        prepareNotificationModel(model, actor, ReceiverType.CUSTOMER, view, "/customer/notifications", "customer", null);
        return "customer/notifications/index";
    }

    @GetMapping("/customer/notifications/{recipientId}")
    public String customerNotificationDetails(Model model, @PathVariable Long recipientId) {
        ManualCommunicationActor actor = permissionService.requireCustomerActor();
        CommunicationRecipient selected = notificationService.markAsRead(recipientId, actor.getActorUserId(), ReceiverType.CUSTOMER);
        prepareNotificationModel(model, actor, ReceiverType.CUSTOMER, "all", "/customer/notifications", "customer", selected);
        return "customer/notifications/index";
    }

    @GetMapping("/vendor/notifications")
    public String vendorNotifications(Model model, @RequestParam(defaultValue = "all") String view) {
        ManualCommunicationActor actor = permissionService.requireVendorActor();
        prepareNotificationModel(model, actor, ReceiverType.VENDOR, view, "/vendor/notifications", "vendor", null);
        return "vendor/notifications/index";
    }

    @GetMapping("/vendor/notifications/{recipientId}")
    public String vendorNotificationDetails(Model model, @PathVariable Long recipientId) {
        ManualCommunicationActor actor = permissionService.requireVendorActor();
        CommunicationRecipient selected = notificationService.markAsRead(recipientId, actor.getActorUserId(), ReceiverType.VENDOR);
        prepareNotificationModel(model, actor, ReceiverType.VENDOR, "all", "/vendor/notifications", "vendor", selected);
        return "vendor/notifications/index";
    }

    @GetMapping("/admin/notifications")
    public String adminNotifications(Model model, @RequestParam(defaultValue = "all") String view) {
        ManualCommunicationActor actor = permissionService.requireAdminActor();
        prepareNotificationModel(model, actor, ReceiverType.ADMIN, view, "/admin/notifications", "admin", null);
        return "admin/notifications/index";
    }

    @GetMapping("/admin/notifications/{recipientId}")
    public String adminNotificationDetails(Model model, @PathVariable Long recipientId) {
        ManualCommunicationActor actor = permissionService.requireAdminActor();
        CommunicationRecipient selected = notificationService.markAsRead(recipientId, actor.getActorUserId(), ReceiverType.ADMIN);
        prepareNotificationModel(model, actor, ReceiverType.ADMIN, "all", "/admin/notifications", "admin", selected);
        return "admin/notifications/index";
    }

    private void prepareNotificationModel(
            Model model,
            ManualCommunicationActor actor,
            ReceiverType receiverType,
            String view,
            String basePath,
            String scope,
            CommunicationRecipient selectedNotification) {
        Long userId = actor.getActorUserId();
        model.addAttribute("notifications", notificationService.findNotifications(userId, receiverType, view));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(userId, receiverType));
        model.addAttribute("activeView", view == null ? "all" : view);
        model.addAttribute("basePath", basePath);
        model.addAttribute("scope", scope);
        model.addAttribute("selectedNotification", selectedNotification);
        model.addAttribute("pageTitle", "Notifications");
    }
}
