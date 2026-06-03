package com.dermatt.rabbitmqpatterns.model;

import java.time.Instant;

public class OrderCreatedEvent {

    private String eventId;
    private String eventType = "PedidoCreado";
    private String occurredAt;
    private String source = "orders-api";
    private Payload payload;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String eventId, String orderId, String customerId, double total) {
        this.eventId = eventId;
        this.occurredAt = Instant.now().toString();
        this.payload = new Payload(orderId, customerId, total);
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getOccurredAt() { return occurredAt; }
    public String getSource() { return source; }
    public Payload getPayload() { return payload; }

    public static class Payload {
        private String orderId;
        private String customerId;
        private double total;

        public Payload() {}

        public Payload(String orderId, String customerId, double total) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.total = total;
        }

        public String getOrderId() { return orderId; }
        public String getCustomerId() { return customerId; }
        public double getTotal() { return total; }
    }
}
