package com.dermatt.rabbitmqpatterns.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final ProducerTemplate producerTemplate;

    public OrdersController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        producerTemplate.sendBody("direct:validate", body);
        return ResponseEntity.accepted().body("Mensaje recibido y procesado");
    }
}
