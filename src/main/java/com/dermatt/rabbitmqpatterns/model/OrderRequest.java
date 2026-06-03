package com.dermatt.rabbitmqpatterns.model;

public class OrderRequest {

    private String orderId;
    private String customerId;
    private double total;

    public OrderRequest() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
