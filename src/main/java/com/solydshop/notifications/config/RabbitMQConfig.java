package com.solydshop.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This service consumes notification-created events, so it owns the full
 * topology: the exchange (redeclared idempotently in case this service
 * starts before the monolith), the durable queue, and the binding between
 * them.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${notifications.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${notifications.rabbitmq.queue}")
    private String queueName;

    @Value("${notifications.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificationsCreateQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsCreateQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsCreateQueue).to(notificationsExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
