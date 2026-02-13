package com.dmcdoc.usermanagement.kds.event;

import com.dmcdoc.usermanagement.kds.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher pour envoyer les événements liés aux commandes sur RabbitMQ.
 *
 * Utilise :
 * - 1 exchange centralisé
 * - 2 routing keys
 */
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publie un événement de création de commande.
     */
    public void publishOrderCreated(OrderCreatedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ROUTING_CREATED,
                event);
    }

    /**
     * Publie un événement de mise à jour de statut.
     */
    public void publishOrderStatusUpdated(OrderStatusUpdatedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ROUTING_STATUS,
                event);
    }
}
