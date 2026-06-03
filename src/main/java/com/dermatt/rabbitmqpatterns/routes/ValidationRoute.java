package com.dermatt.rabbitmqpatterns.routes;

import com.dermatt.rabbitmqpatterns.model.InvalidMessage;
import com.dermatt.rabbitmqpatterns.model.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ValidationRoute extends RouteBuilder {

    private final ObjectMapper mapper;

    public ValidationRoute(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure() {

        from("direct:validate")
            .routeId("validation-route")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                try {
                    OrderRequest req = mapper.readValue(body, OrderRequest.class);
                    String error = null;
                    if (req.getOrderId() == null || req.getOrderId().isBlank()) {
                        error = "orderId es requerido";
                    } else if (req.getCustomerId() == null || req.getCustomerId().isBlank()) {
                        error = "customerId es requerido";
                    } else if (req.getTotal() <= 0) {
                        error = "total debe ser mayor a 0";
                    }
                    if (error != null) {
                        exchange.getIn().setHeader("validationError", error);
                    } else {
                        exchange.getIn().setBody(req);
                    }
                } catch (Exception e) {
                    exchange.getIn().setHeader("validationError", "JSON invalido: " + e.getMessage());
                }
            })
            .choice()
                .when(header("validationError").isNotNull())
                    .process(exchange -> {
                        String reason = exchange.getIn().getHeader("validationError", String.class);
                        String original = exchange.getIn().getBody(String.class);
                        exchange.getIn().setBody(mapper.writeValueAsString(new InvalidMessage(reason, original)));
                    })
                    .to("spring-rabbitmq:invalid.exchange?routingKey=invalid")
                    .log("[validation-route] Mensaje invalido enviado a invalid-message.queue: ${header.validationError}")
                .otherwise()
                    .to("direct:dispatchOrder")
            .end();

        from("direct:dispatchOrder")
            .routeId("dispatch-route")
            .multicast().parallelProcessing()
                .to("direct:billingDispatch", "direct:eventDispatch")
            .end();
    }
}
