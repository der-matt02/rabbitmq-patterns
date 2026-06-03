package com.dermatt.rabbitmqpatterns.routes;

import com.dermatt.rabbitmqpatterns.model.BillingCommand;
import com.dermatt.rabbitmqpatterns.model.OrderCreatedEvent;
import com.dermatt.rabbitmqpatterns.model.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrdersRoute extends RouteBuilder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure() {

        // Parte A: Point-to-Point — envia Command Message a billing.queue
        from("direct:billingDispatch")
            .routeId("billing-dispatch-route")
            .process(exchange -> {
                OrderRequest req = exchange.getIn().getBody(OrderRequest.class);
                String msgId = "msg-" + UUID.randomUUID().toString().substring(0, 8);
                BillingCommand cmd = new BillingCommand(msgId, req.getOrderId(), req.getCustomerId(), req.getTotal());
                exchange.getIn().setBody(mapper.writeValueAsString(cmd));
            })
            .to("spring-rabbitmq:billing.exchange?routingKey=billing")
            .log("[orders-route] BillingCommand enviado a billing.queue");

        // Parte B: Publish/Subscribe — publica Event Message en orders.exchange (fanout)
        from("direct:eventDispatch")
            .routeId("event-dispatch-route")
            .process(exchange -> {
                OrderRequest req = exchange.getIn().getBody(OrderRequest.class);
                String evtId = "evt-" + UUID.randomUUID().toString().substring(0, 8);
                OrderCreatedEvent event = new OrderCreatedEvent(evtId, req.getOrderId(), req.getCustomerId(), req.getTotal());
                exchange.getIn().setBody(mapper.writeValueAsString(event));
            })
            .to("spring-rabbitmq:orders.exchange?exchangeType=fanout")
            .log("[orders-route] PedidoCreado publicado en orders.exchange");
    }
}
