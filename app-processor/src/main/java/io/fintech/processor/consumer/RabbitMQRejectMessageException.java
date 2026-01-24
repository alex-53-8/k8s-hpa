package io.fintech.processor.consumer;

public class RabbitMQRejectMessageException extends RuntimeException {
    public RabbitMQRejectMessageException(String message) {
        super(message);
    }
}
