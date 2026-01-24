package io.fintech.processor.consumer;

import io.fintech.processor.job.JobDescription;
import io.fintech.processor.job.ProcessorQueueHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitMQListener implements SignalListener {

    private final ObjectMapper mapper;

    private final Consumer consumer;

    private final RabbitListenerEndpointRegistry registry;

    public RabbitMQListener(RabbitListenerEndpointRegistry registry,
                            Consumer consumer,
                            ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.mapper = objectMapper;
        this.registry = registry;
    }

    @RabbitListener(id = "processor-tasks-listener", queues = "${spring.rabbitmq.queue}")
    public void listen(String message) {
        JobDescription jobDescription = mapper.readValue(message, JobDescription.class);
        consumer.consume(jobDescription, this);
    }

    public void nack(JobDescription jobDescription) {
        throw new RabbitMQRejectMessageException(
                "the job %s must return back to the queue".formatted(jobDescription.jobId().toString())
        );
    }

    public void start() {
        registry.getListenerContainer("processor-tasks-listener").start();
    }

    public void stop() {
        registry.getListenerContainer("processor-tasks-listener").stop();
    }

}