package com.dermatt.rabbitmqpatterns.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

// Parte A: Point-to-Point Channel — un solo consumidor procesa cada mensaje de facturacion
@Component
public class BillingRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("spring-rabbitmq:billing.exchange?queues=billing.queue&routingKey=billing")
            .routeId("billing-consumer-route")
            .log("[billing-service] Procesando factura: ${body}");
    }
}
