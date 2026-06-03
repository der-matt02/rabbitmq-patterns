package com.dermatt.rabbitmqpatterns.model;

public class InvalidMessage {

    private String reason;
    private String originalBody;

    public InvalidMessage() {}

    public InvalidMessage(String reason, String originalBody) {
        this.reason = reason;
        this.originalBody = originalBody;
    }

    public String getReason() { return reason; }
    public String getOriginalBody() { return originalBody; }
}
