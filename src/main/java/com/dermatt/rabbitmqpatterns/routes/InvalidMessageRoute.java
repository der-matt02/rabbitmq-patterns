package com.dermatt.rabbitmqpatterns.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

// Parte C: Invalid Message Channel — consume mensajes invalidos o fallidos
@Component
public class InvalidMessageRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("spring-rabbitmq:invalid.exchange?queues=invalid-message.queue&routingKey=invalid")
            .routeId("invalid-message-consumer-route")
            .log("[error-handler] Mensaje invalido recibido en invalid-message.queue: ${body}");
    }
}
