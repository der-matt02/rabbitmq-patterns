# Evidencias de Ejecución (Logs)

A continuación se presentan los registros (logs) del sistema que demuestran el correcto funcionamiento de los patrones de integración implementados, tal como se solicita en los entregables del taller. Los logs han sido limpiados para destacar únicamente el comportamiento de la mensajería.

---

## Caso 1: Facturación Válida (Patrón Point-to-Point)
Se envía un comando válido de facturación. El log demuestra que el mensaje es despachado a la cola exclusiva y procesado por un único consumidor (`billing-service`).

```text
2026-06-07T22:45:33.351-05:00 INFO --- [ #2 - Multicast] billing-dispatch-route : [orders-route] BillingCommand enviado a billing.queue
2026-06-07T22:45:33.361-05:00 INFO --- [pool-2-thread-7] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-96ad8a5c","messageType":"GenerarFactura","orderId":"ORD-1001","customerId":"CLI-2001","total":59.9}
```

---

## Caso 2: Evento PedidoCreado (Patrón Publish/Subscribe)
Se publica un evento de creación de pedido. El log demuestra que el mensaje se distribuye mediante un exchange tipo *fanout* y es consumido de forma simultánea por dos servicios diferentes (`notification-service` y `analytics-service`).

```text
2026-06-07T22:45:33.351-05:00 INFO --- [ #4 - Multicast] event-dispatch-route     : [orders-route] PedidoCreado publicado en orders.exchange
2026-06-07T22:45:33.361-05:00 INFO --- [pool-2-thread-8] notification-consumer-route: [notification-service] Notificacion enviada al cliente: {"eventId":"evt-331273ec","eventType":"PedidoCreado","occurredAt":"2026-06-08T03:45:33.317280900Z","source":"orders-api","payload":{"orderId":"ORD-1001","customerId":"CLI-2001","total":59.9}}
2026-06-07T22:45:33.361-05:00 INFO --- [pool-2-thread-9] analytics-consumer-route   : [analytics-service] Evento registrado para analitica: {"eventId":"evt-331273ec","eventType":"PedidoCreado","occurredAt":"2026-06-08T03:45:33.317280900Z","source":"orders-api","payload":{"orderId":"ORD-1001","customerId":"CLI-2001","total":59.9}}
```

---

## Casos 3 y 4: Manejo de Errores e Inválidos (Invalid Message Channel)
Se inyectan mensajes sin los campos obligatorios o con valores inválidos. La validación falla y el mensaje es desviado a la cola de errores sin interrumpir los demás flujos. Un consumidor de errores (`error-handler`) procesa el mensaje muerto.

```text
2026-06-07T22:47:23.753-05:00 INFO --- [nio-8080-exec-3] validation-route               : [validation-route] Mensaje invalido enviado a invalid-message.queue: orderId es requerido
2026-06-07T22:47:23.756-05:00 INFO --- [ool-2-thread-10] invalid-message-consumer-route : [error-handler] Mensaje invalido recibido en invalid-message.queue: {"reason":"orderId es requerido","originalBody":"{\n  \"eventId\": \"evt-001\",\n  \"eventType\": \"PedidoCreado\" ...}"}
```

---

## Caso 5: Competencia de Consumidores (Point-to-Point bajo carga)
Se envían múltiples peticiones válidas en ráfaga. El sistema levanta dinámicamente varios consumidores concurrentes (threads) para repartirse la carga. El log demuestra que los mensajes se procesaron de manera distribuida y **ningún mensaje fue procesado dos veces**, demostrando un correcto balanceo de carga en la cola.

```text
2026-06-07T22:54:16.659-05:00 INFO --- [pool-2-thread-3] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-9c654d58","orderId":"ORD-1010","total":100.0}
2026-06-07T22:54:19.333-05:00 INFO --- [pool-2-thread-6] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-17ae4b0b","orderId":"ORD-1010","total":100.0}
2026-06-07T22:54:20.509-05:00 INFO --- [pool-2-thread-9] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-a1830413","orderId":"ORD-1010","total":100.0}
2026-06-07T22:54:21.547-05:00 INFO --- [ool-2-thread-13] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-f74f77b3","orderId":"ORD-1010","total":100.0}
2026-06-07T22:54:22.628-05:00 INFO --- [pool-2-thread-4] billing-consumer-route : [billing-service] Procesando factura: {"messageId":"msg-3465881c","orderId":"ORD-1010","total":100.0}
```
