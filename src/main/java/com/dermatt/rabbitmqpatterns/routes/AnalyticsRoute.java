package com.dermatt.rabbitmqpatterns.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

// Parte B: Publish/Subscribe — suscriptor de analitica
@Component
public class AnalyticsRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("spring-rabbitmq:orders.exchange?queues=analytics.queue&exchangeType=fanout")
            .routeId("analytics-consumer-route")
            .log("[analytics-service] Evento registrado para analitica: ${body}");
    }
}
