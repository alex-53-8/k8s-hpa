package io.fintech.processor.job;

import io.fintech.processor.consumer.Consumer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProcessorQueueHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorQueueHandler.class);

    private final AtomicInteger threadsUsed = new AtomicInteger(0);

    private final ExecutorService executor;

    private final int maxThreadCount;

    public ProcessorQueueHandler(
        MeterRegistry registry,
        @Value("${processor.max-threads}") int maxThreadCount,
        @Value("${processor.metrics-names.used-threads.name}") String threadsUsedMetricsName,
        @Value("${processor.metrics-names.used-threads.descr}") String threadsUsedMetricsDescription
    ) {
        logger.info("Creating queue handler with {} threads", maxThreadCount);

        this.maxThreadCount = maxThreadCount;
        this.executor = Executors.newFixedThreadPool(maxThreadCount);

        Gauge.builder(threadsUsedMetricsName, () -> threadsUsed)
                .description(threadsUsedMetricsDescription)
                .register(registry);
    }

    public void submit(JobDescription description, Consumer consumer) {
        Objects.requireNonNull(description);

        if (threadsUsed.get() >= maxThreadCount) {
            throw new CapacityExceededException("Maximum number of threads reached");
        }

        threadsUsed.incrementAndGet();

        executor.submit(() -> {
            try {
                new JobWorker(description).process();
            } finally {
                threadsUsed.decrementAndGet();
                consumer.start();
            }
        });

        // we do not want to submit more tasks than we have free workers
        if (threadsUsed.get() >= maxThreadCount) {
            try {
                consumer.stop();
            } catch (Exception e) {
                logger.error("Error while stopping a consumer", e);
            }
        }
    }

}