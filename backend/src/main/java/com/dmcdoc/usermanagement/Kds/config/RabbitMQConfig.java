package com.dmcdoc.usermanagement.kds.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "kds.orders.exchange";

    public static final String ORDER_CREATED_QUEUE = "orders.created.queue";
    public static final String ORDER_STATUS_QUEUE = "orders.status.queue";

    public static final String ORDER_DLX = "kds.orders.dlx";
    public static final String ORDER_DLQ = "orders.dlq";

    public static final String ROUTING_CREATED = "order.created";
    public static final String ROUTING_STATUS = "order.status.updated";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(ORDER_DLX, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    @Bean
    public Binding bindingCreated(
            @Qualifier("orderCreatedQueue") Queue orderCreatedQueue,
            @Qualifier("orderExchange") TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(ROUTING_CREATED);
    }

    @Bean
    public Binding bindingStatus(
            @Qualifier("orderStatusQueue") Queue orderStatusQueue,
            @Qualifier("orderExchange") TopicExchange orderExchange) {
        return BindingBuilder.bind(orderStatusQueue)
                .to(orderExchange)
                .with(ROUTING_STATUS);
    }

    @Bean
    public Binding bindingDLQ(
            @Qualifier("deadLetterQueue") Queue deadLetterQueue,
            @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("#");
    }
}

