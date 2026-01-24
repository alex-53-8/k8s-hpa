package io.fintech.processor.consumer;

import io.fintech.processor.job.JobDescription;
import io.fintech.processor.job.CapacityExceededException;
import io.fintech.processor.job.ProcessorQueueHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class Consumer {

    private final ProcessorQueueHandler handler;

    private final ApplicationContext applicationContext;

    public Consumer(ProcessorQueueHandler handler, ApplicationContext applicationContext) {
        this.handler = handler;
        this.applicationContext = applicationContext;
    }

    private Collection<SignalListener> getSignalListeners() {
        return applicationContext.getBeansOfType(SignalListener.class).values();
    }

    public void start() {
        getSignalListeners().forEach(SignalListener::start);
    }

    public void stop() {
        getSignalListeners().forEach(SignalListener::stop);
    }

    public void consume(JobDescription description, SignalListener source) {
        try {
            handler.submit(description, this);
        } catch (CapacityExceededException e) {
            source.nack(description);
        }
    }

}
