package com.dermatt.rabbitmqpatterns.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

// Parte B: Publish/Subscribe — suscriptor de notificaciones
@Component
public class NotificationRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("spring-rabbitmq:orders.exchange?queues=notification.queue")
            .routeId("notification-consumer-route")
            .log("[notification-service] Notificacion enviada al cliente: ${body}");
    }
}
