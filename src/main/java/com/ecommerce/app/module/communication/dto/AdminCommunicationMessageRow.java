package com.ecommerce.app.module.communication.dto;

import com.ecommerce.app.module.communication.model.CommunicationMessage;

public record AdminCommunicationMessageRow(
        CommunicationMessage message,
        MessageReadReport report
) {}
