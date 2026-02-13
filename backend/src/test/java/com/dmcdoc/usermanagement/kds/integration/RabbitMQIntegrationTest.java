package com.dmcdoc.usermanagement.kds.integration;

import com.dmcdoc.usermanagement.kds.config.RabbitMQConfig;
import com.dmcdoc.usermanagement.kds.event.OrderCreatedEvent;
import com.dmcdoc.usermanagement.kds.event.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @Autowired
    private OrderEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldSendOrderCreatedEventToRabbitMQ() {

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .restaurantId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .total(BigDecimal.TEN)
                .items(List.of())
                .build();

        publisher.publishOrderCreated(event);

        Object received = rabbitTemplate.receiveAndConvert(
                RabbitMQConfig.ORDER_CREATED_QUEUE,
                5000);

        assertThat(received).isNotNull();
        assertThat(received).isInstanceOf(OrderCreatedEvent.class);

        OrderCreatedEvent receivedEvent = (OrderCreatedEvent) received;
        assertThat(receivedEvent.getOrderId()).isEqualTo(event.getOrderId());
    }
}
/*
 * 🎯 Ce que ce test valide réellement
 * 
 * ✔ RabbitMQ container démarre
 * ✔ Spring se connecte au broker
 * ✔ Exchange + Queue déclarés
 * ✔ Message réellement publié
 * ✔ Message réellement consommé
 * 
 * C’est un vrai test infra.
 * 
 * ⚠️ Important
 * 
 * Docker doit être actif.
 */