package com.dermatt.rabbitmqpatterns.model;

public class BillingCommand {

    private String messageId;
    private String messageType = "GenerarFactura";
    private String orderId;
    private String customerId;
    private double total;

    public BillingCommand() {}

    public BillingCommand(String messageId, String orderId, String customerId, double total) {
        this.messageId = messageId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
    }

    public String getMessageId() { return messageId; }
    public String getMessageType() { return messageType; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getTotal() { return total; }
}
