package com.dermatt.rabbitmqpatterns.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Point-to-Point: facturación ---
    @Bean
    public Queue billingQueue() {
        return QueueBuilder.durable("billing.queue").build();
    }

    @Bean
    public DirectExchange billingExchange() {
        return new DirectExchange("billing.exchange");
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, DirectExchange billingExchange) {
        return BindingBuilder.bind(billingQueue).to(billingExchange).with("billing");
    }

    // --- Publish/Subscribe: PedidoCreado ---
    @Bean
    public FanoutExchange ordersExchange() {
        return new FanoutExchange("orders.exchange");
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("notification.queue").build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable("analytics.queue").build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, FanoutExchange ordersExchange) {
        return BindingBuilder.bind(notificationQueue).to(ordersExchange);
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, FanoutExchange ordersExchange) {
        return BindingBuilder.bind(analyticsQueue).to(ordersExchange);
    }

    // --- Invalid Message Channel ---
    @Bean
    public Queue invalidMessageQueue() {
        return QueueBuilder.durable("invalid-message.queue").build();
    }

    @Bean
    public DirectExchange invalidExchange() {
        return new DirectExchange("invalid.exchange");
    }

    @Bean
    public Binding invalidBinding(Queue invalidMessageQueue, DirectExchange invalidExchange) {
        return BindingBuilder.bind(invalidMessageQueue).to(invalidExchange).with("invalid");
    }
}
