package io.fintech.processor.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue(@Value("${spring.rabbitmq.queue}") String queueName) {
        return new Queue(queueName, false);
    }

    @Bean
    public TopicExchange exchange(@Value("${spring.rabbitmq.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(
            Queue queue,
            TopicExchange exchange,
            @Value("${spring.rabbitmq.routing-key-pattern}") String routingKeyPattern
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKeyPattern);
    }

}