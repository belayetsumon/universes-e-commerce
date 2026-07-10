package com.ecommerce.app.module.communication.model;

public enum DeliveryMode {
    DIRECT("Direct"),
    DATABASE_QUEUE("Database Queue"),
    RABBITMQ("RabbitMQ"),
    KAFKA("Kafka");

    private final String displayName;

    DeliveryMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
