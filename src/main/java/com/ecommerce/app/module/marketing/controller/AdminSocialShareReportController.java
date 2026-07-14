package com.ecommerce.app.module.marketing.controller;

import com.ecommerce.app.module.marketing.services.SocialShareTrackingService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/marketing/share-analytics")
public class AdminSocialShareReportController {

    private final SocialShareTrackingService trackingService;

    public AdminSocialShareReportController(SocialShareTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping
    public String report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate start = startDate == null ? end.minusDays(30) : startDate;
        ZoneId zone = ZoneId.systemDefault();
        Instant startInstant = start.atStartOfDay(zone).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(zone).toInstant();

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("summary", trackingService.summary(startInstant, endInstant));
        model.addAttribute("sharesByPlatform", trackingService.byPlatform(startInstant, endInstant));
        model.addAttribute("sharesByPageType", trackingService.byPageType(startInstant, endInstant));
        model.addAttribute("mostSharedProducts", trackingService.topEntities("PRODUCT", startInstant, endInstant));
        model.addAttribute("mostSharedVendors", trackingService.topEntities("VENDOR", startInstant, endInstant));
        return "admin/marketing/share-analytics";
    }
}
