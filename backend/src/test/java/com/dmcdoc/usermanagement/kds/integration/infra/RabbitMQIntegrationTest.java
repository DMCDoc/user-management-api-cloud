package com.dmcdoc.usermanagement.kds.integration.infra;

import com.dmcdoc.usermanagement.kds.config.RabbitMQConfig;
import com.dmcdoc.usermanagement.kds.event.OrderCreatedEvent;
import com.dmcdoc.usermanagement.kds.event.OrderEventPublisher;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ContextConfiguration(classes = {
        RabbitMQConfig.class,
        OrderEventPublisher.class,
        RabbitMQIntegrationTest.RabbitTestConfig.class
})

@Testcontainers

public class RabbitMQIntegrationTest {

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

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    void shouldSendOrderCreatedEventToRabbitMQ() {
        // Ensure declarations are created before publishing/consuming in this minimal test context.
        amqpAdmin.initialize();

        UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
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
        assertThat(receivedEvent)
                .usingRecursiveComparison()
                .isEqualTo(event);

    }

    @Configuration
    static class RabbitTestConfig {

        @Bean
        ConnectionFactory connectionFactory(
                @Value("${spring.rabbitmq.host}") String host,
                @Value("${spring.rabbitmq.port}") int port,
                @Value("${spring.rabbitmq.username}") String username,
                @Value("${spring.rabbitmq.password}") String password) {
            CachingConnectionFactory factory = new CachingConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            return factory;
        }

        @Bean
        Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
        }

        @Bean
        RabbitTemplate rabbitTemplate(
                ConnectionFactory connectionFactory,
                Jackson2JsonMessageConverter messageConverter) {
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(messageConverter);
            return template;
        }

        @Bean
        AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
            return new org.springframework.amqp.rabbit.core.RabbitAdmin(connectionFactory);
        }
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
