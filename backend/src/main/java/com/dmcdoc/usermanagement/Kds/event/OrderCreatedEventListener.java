package com.dmcdoc.usermanagement.kds.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.dmcdoc.usermanagement.kds.config.RabbitMQConfig;

/**
 * Listener pour les commandes créées.
 */
@Component
@Slf4j
public class OrderCreatedEventListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Nouveau order reçu KDS: orderId={}, restaurantId={}, total={}",
                event.getOrderId(), event.getRestaurantId(), event.getTotal());

        // Ici tu peux déclencher la préparation KDS, printer, ou autre logique métier
        // Exemple : kdsService.prepareOrder(event);
    }
}
