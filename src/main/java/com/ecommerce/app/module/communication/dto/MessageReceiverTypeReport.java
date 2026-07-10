package com.ecommerce.app.module.communication.dto;

public record MessageReceiverTypeReport(
        long customerReadCount,
        long customerUnreadCount,
        long vendorReadCount,
        long vendorUnreadCount,
        long adminReadCount,
        long adminUnreadCount
) {}
