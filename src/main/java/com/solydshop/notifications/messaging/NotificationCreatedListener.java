package com.solydshop.notifications.messaging;

import com.solydshop.notifications.dto.CreateNotificationRequest;
import com.solydshop.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * A message that fails to process here is logged and dropped (the listener
 * method returns normally, so Spring AMQP acknowledges it) rather than
 * requeued forever - a poison-message loop is worse than losing one
 * notification. Proper dead-lettering is a deliberate future lesson, not an
 * oversight - see the tracked follow-up issue.
 */
@Component
public class NotificationCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationCreatedListener.class);

    private final NotificationService notificationService;

    public NotificationCreatedListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notifications.rabbitmq.queue}")
    public void handle(CreateNotificationRequest request) {
        try {
            notificationService.createForUser(
                    request.getUserId(), request.getTitle(), request.getMessage(),
                    request.getType(), request.getResourceId());
        } catch (Exception e) {
            log.error("Failed to process notification-created message for user {}: {}",
                    request.getUserId(), e.getMessage());
        }
    }
}
