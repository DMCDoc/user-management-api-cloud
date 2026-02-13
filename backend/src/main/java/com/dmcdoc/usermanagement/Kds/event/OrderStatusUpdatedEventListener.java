package com.dmcdoc.usermanagement.kds.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.dmcdoc.usermanagement.kds.config.RabbitMQConfig;

/**
 * Listener pour les mises à jour de statut de commandes.
 */
@Component
@Slf4j
public class OrderStatusUpdatedEventListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_QUEUE)
    public void handleOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        log.info("Order status mis à jour KDS: orderId={}, newStatus={}",
                event.getOrderId(), event.getStatus());

        // Ici tu peux déclencher une action spécifique selon le statut
        // Exemple : notifier le front, déclencher une préparation ou annulation
    }
}
