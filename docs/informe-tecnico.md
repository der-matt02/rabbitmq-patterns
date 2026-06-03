# **Informe Técnico — Taller Semana 8**
## **Patrones básicos de mensajería con RabbitMQ y Apache Camel**

**Materia:** Integración de Sistemas
**Docente:** Darío Villamarín G.
**Integrante(s):** [Escribir nombre(s)]
**Fecha:** junio 2026

> *Nota de formato: todo el documento debe estar en Arial 12, color negro.
> Títulos de sección en Arial 14 negrita. Subtítulos en Arial 12 negrita.*

---

## **1. Introducción**

El presente informe documenta la implementación del Taller Semana 8 de la materia Integración de Sistemas, cuyo objetivo es aplicar patrones básicos de mensajería empresarial utilizando RabbitMQ como broker de mensajes y Apache Camel como motor de integración. El caso de negocio simula una tienda en línea que, al recibir un pedido, debe coordinar de forma asíncrona los procesos de facturación, notificación al cliente y registro analítico, sin crear dependencias directas entre estos sistemas.

---

## **2. Desarrollo**

### **2.1 Arquitectura implementada**

La solución se construyó como una única aplicación Spring Boot 4.0.6 que contiene todos los componentes simulados. RabbitMQ se ejecuta en Docker mediante `docker-compose.yml`. Apache Camel define seis rutas de integración con responsabilidades claramente separadas.

**[INSERTAR IMAGEN — ANEXO 1: Diagrama de arquitectura exportado desde Mermaid (archivo docs/arquitectura.mmd)]**

### **2.2 Patrón Point-to-Point — Facturación (Parte A)**

El patrón Point-to-Point Channel se aplicó en el flujo de facturación. Cuando un pedido es válido, la ruta `billing-dispatch-route` construye un **Command Message** de tipo `GenerarFactura` y lo publica en `billing.exchange` (exchange direct) con routing key `billing`. Este mensaje es enrutado a `billing.queue`, donde únicamente `billing-service` lo consume.

La razón de usar Point-to-Point es que la generación de una factura es una operación transaccional que debe ejecutarse exactamente una vez. Si existieran dos instancias de `billing-service`, RabbitMQ garantiza que solo una tomará cada mensaje, evitando facturación duplicada.

El **Command Message** tiene la siguiente estructura:

```json
{
  "messageId": "msg-a1b2c3d4",
  "messageType": "GenerarFactura",
  "orderId": "ORD-1001",
  "customerId": "CLI-2001",
  "total": 59.90
}
```

**[INSERTAR IMAGEN — ANEXO 2: Captura de RabbitMQ Management → pestaña Queues mostrando billing.queue]**

**[INSERTAR IMAGEN — ANEXO 3: Log de billing-service procesando el BillingCommand]**

### **2.3 Patrón Publish/Subscribe — PedidoCreado (Parte B)**

El patrón Publish/Subscribe Channel se aplicó para la distribución del evento de pedido. Simultáneamente al envío del Command Message, la ruta `event-dispatch-route` construye un **Event Message** de tipo `PedidoCreado` y lo publica en `orders.exchange` (exchange de tipo fanout).

Al ser fanout, RabbitMQ distribuye automáticamente una copia del mensaje a todas las colas suscritas: `notification.queue` y `analytics.queue`. Cada una tiene su propio consumidor (`notification-service` y `analytics-service` respectivamente), por lo que ambos reciben el mismo evento de forma independiente.

La razón de usar Publish/Subscribe es que tanto la notificación como el registro analítico son sistemas independientes que deben enterarse del mismo hecho (el pedido fue creado) sin conocerse entre sí.

El **Event Message** tiene la siguiente estructura:

```json
{
  "eventId": "evt-e5f6a7b8",
  "eventType": "PedidoCreado",
  "occurredAt": "2026-06-03T17:00:00Z",
  "source": "orders-api",
  "payload": {
    "orderId": "ORD-1001",
    "customerId": "CLI-2001",
    "total": 59.90
  }
}
```

**[INSERTAR IMAGEN — ANEXO 4: Captura de RabbitMQ Management → pestaña Exchanges mostrando orders.exchange (fanout) y billing.exchange (direct)]**

**[INSERTAR IMAGEN — ANEXO 5: Logs de notification-service y analytics-service recibiendo el mismo evento PedidoCreado]**

### **2.4 Validación y manejo de errores (Parte C)**

La ruta `validation-route` es el punto de entrada único para todos los mensajes. Antes de enrutar, valida las siguientes condiciones:

- `orderId` no es nulo ni vacío
- `customerId` no es nulo ni vacío
- `total` es mayor a cero
- El cuerpo del mensaje es JSON válido

Si alguna condición falla, el mensaje se serializa junto con la razón del error en un objeto `InvalidMessage` y se envía a `invalid.exchange`, que lo deposita en `invalid-message.queue`. La ruta `invalid-message-consumer-route` consume estos mensajes y los registra en el log del sistema.

**[INSERTAR IMAGEN — ANEXO 6: Log de error-handler mostrando un mensaje inválido (ej: sin orderId) recibido en invalid-message.queue]**

### **2.5 Análisis de escenarios**

**¿Qué pasaría si billing-service procesa dos veces el mismo pedido?**
Generaría una factura duplicada. Para prevenir esto en producción se debería implementar idempotencia mediante un registro de `messageId` procesados en base de datos, rechazando mensajes ya procesados.

**¿Qué pasaría si analytics-service está caído?**
Los mensajes se acumularían en `analytics.queue` hasta que el servicio se recupere. RabbitMQ los retiene. Notification-service no se ve afectado porque opera sobre su propia cola independiente.

**¿El pedido debería fallar si no se pudo enviar la notificación?**
No. La notificación es un efecto secundario del pedido, no una condición para su éxito. El pedido ya fue aceptado y la factura ya fue generada. Acoplar el éxito del pedido a la notificación crearía una dependencia rígida innecesaria, contradiciendo el principio de desacoplamiento que motiva el uso de mensajería asíncrona.

**[INSERTAR IMAGEN — ANEXO 7: Captura de la ejecución de mvn test mostrando 7/7 tests en verde (BUILD SUCCESS)]**

---

## **3. Conclusiones**

La implementación demostró que los patrones Point-to-Point y Publish/Subscribe resuelven necesidades fundamentalmente distintas: el primero garantiza procesamiento exclusivo para operaciones críticas como la facturación, mientras el segundo permite distribuir eventos a múltiples sistemas sin acoplamiento. Apache Camel facilitó la definición declarativa de estas rutas, haciendo el flujo de integración legible y mantenible.

Para llevar esta solución a producción se recomendaría: (1) implementar idempotencia en `billing-service` para evitar facturación duplicada ante re-entregas; (2) configurar Dead Letter Exchanges con reintentos automáticos para mensajes que fallen por errores transitorios; (3) añadir un Dead Letter Queue para mensajes que agoten los reintentos; (4) implementar monitoreo de profundidad de colas para detectar servicios caídos; y (5) agregar autenticación en RabbitMQ y cifrado TLS en la comunicación.

---

## **4. Referencias**

- Hohpe, G. & Woolf, B. (2004). *Enterprise Integration Patterns: Designing, Building, and Deploying Messaging Solutions*. Addison-Wesley. Capítulos 3, 4 y 5.
- Richardson, C. (2019). *Microservices Patterns: With Examples in Java*. Manning. Capítulo 3.
- RabbitMQ. (2024). *Exchanges, Queues, Bindings and Dead Letter Exchanges*. https://www.rabbitmq.com/documentation.html
- Apache Camel. (2024). *Spring RabbitMQ Component*. https://camel.apache.org/components/latest/spring-rabbitmq-component.html
- Spring Framework. (2024). *Spring AMQP Reference Documentation*. https://spring.io/projects/spring-amqp

---

*Fin del informe técnico*
