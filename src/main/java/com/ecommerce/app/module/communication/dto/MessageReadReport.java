package com.ecommerce.app.module.communication.dto;

public record MessageReadReport(
        long totalRecipients,
        long readCount,
        long unreadCount,
        double readPercentage
) {}
