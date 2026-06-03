package com.dermatt.rabbitmqpatterns;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Requiere RabbitMQ corriendo: docker compose up -d
@SpringBootTest
class OrdersIntegrationTest {

    @Autowired
    ProducerTemplate producerTemplate;

    @Autowired
    CamelContext camelContext;

    // --- Caso 1: Facturacion valida procesada por billing-service (Point-to-Point) ---
    @Test
    void caso1_facturacion_valida_procesada_por_billingService() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("billing-consumer-route").whenDone(1).create();

        producerTemplate.sendBody("direct:validate",
                "{\"orderId\":\"ORD-1001\",\"customerId\":\"CLI-2001\",\"total\":59.90}");

        assertTrue(notify.matches(5, TimeUnit.SECONDS),
                "billing-service debio procesar el BillingCommand en billing.queue");
    }

    // --- Caso 2: Evento PedidoCreado recibido por notification-service Y analytics-service (Pub/Sub) ---
    @Test
    void caso2_evento_pedido_recibido_por_notification_y_analytics() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("notification-consumer-route").whenDone(1)
                .and()
                .fromRoute("analytics-consumer-route").whenDone(1)
                .create();

        producerTemplate.sendBody("direct:validate",
                "{\"orderId\":\"ORD-1002\",\"customerId\":\"CLI-2002\",\"total\":120.00}");

        assertTrue(notify.matches(5, TimeUnit.SECONDS),
                "notification-service y analytics-service debieron recibir el mismo evento PedidoCreado");
    }

    // --- Caso 3: Mensaje sin orderId enrutado a invalid-message.queue ---
    @Test
    void caso3_mensaje_sin_orderId_enviado_a_invalid_queue() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("invalid-message-consumer-route").whenDone(1).create();

        producerTemplate.sendBody("direct:validate",
                "{\"customerId\":\"CLI-2003\",\"total\":50.00}");

        assertTrue(notify.matches(5, TimeUnit.SECONDS),
                "El mensaje sin orderId debio enrutarse a invalid-message.queue");
    }

    // --- Caso 4: Total igual a 0 enrutado a invalid-message.queue ---
    @Test
    void caso4_total_cero_enviado_a_invalid_queue() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("invalid-message-consumer-route").whenDone(1).create();

        producerTemplate.sendBody("direct:validate",
                "{\"orderId\":\"ORD-1004\",\"customerId\":\"CLI-2004\",\"total\":0}");

        assertTrue(notify.matches(5, TimeUnit.SECONDS),
                "El mensaje con total=0 debio enrutarse a invalid-message.queue");
    }

    // --- Caso extra: JSON malformado enrutado a invalid-message.queue ---
    @Test
    void json_invalido_enviado_a_invalid_queue() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("invalid-message-consumer-route").whenDone(1).create();

        producerTemplate.sendBody("direct:validate", "esto no es json valido");

        assertTrue(notify.matches(5, TimeUnit.SECONDS),
                "JSON malformado debio enrutarse a invalid-message.queue");
    }

    // --- Verifica que un pedido valido activa billing (P2P) y pubsub simultaneamente ---
    @Test
    void pedido_valido_activa_billing_y_pubsub_simultaneamente() {
        NotifyBuilder notify = new NotifyBuilder(camelContext)
                .fromRoute("billing-consumer-route").whenDone(1)
                .and()
                .fromRoute("notification-consumer-route").whenDone(1)
                .and()
                .fromRoute("analytics-consumer-route").whenDone(1)
                .create();

        producerTemplate.sendBody("direct:validate",
                "{\"orderId\":\"ORD-1005\",\"customerId\":\"CLI-2005\",\"total\":99.99}");

        assertTrue(notify.matches(8, TimeUnit.SECONDS),
                "Un pedido valido debe activar billing (P2P), notification y analytics (PubSub)");
    }
}
