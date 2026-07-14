package com.ecommerce.app.module.marketing.dto;

public record SocialShareReportSummary(
        long totalShares,
        long referralShares,
        long registeredShares,
        long guestShares
) {
}
