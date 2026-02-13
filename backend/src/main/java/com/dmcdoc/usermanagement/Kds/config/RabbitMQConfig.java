package com.dmcdoc.usermanagement.kds.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "kds.orders.exchange";

    public static final String ORDER_CREATED_QUEUE = "kds.orders.created.queue";
    public static final String ORDER_STATUS_QUEUE = "kds.orders.status.queue";

    public static final String ROUTING_CREATED = "order.created";
    public static final String ROUTING_STATUS = "order.status.updated";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE).build();
    }

    @Bean
    public Binding bindingCreated(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(ROUTING_CREATED);
    }

    @Bean
    public Binding bindingStatus(Queue orderStatusQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderStatusQueue)
                .to(orderExchange)
                .with(ROUTING_STATUS);
    }
}
