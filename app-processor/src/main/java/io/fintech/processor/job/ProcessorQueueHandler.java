package io.fintech.processor.job;

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

    private final ExecutorService executor;

    private final AtomicInteger threadsUsed;

    public ProcessorQueueHandler(@Value("${processor.max-threads}") int threadCount,
                                 MeterRegistry registry,
                                 @Value("${processor.metrics-names.used-threads.name}") String threadsUsedMetricsName,
                                 @Value("${processor.metrics-names.used-threads.descr}") String threadsUsedMetricsDescription
    ) {
        logger.info("Creating queue handler with {} threads", threadCount);
        executor = Executors.newFixedThreadPool(threadCount);

        threadsUsed = new AtomicInteger(0);
        Gauge.builder(threadsUsedMetricsName, () -> threadsUsed)
                .description(threadsUsedMetricsDescription)
                .register(registry);
    }

    public void submit(JobDescription description) {
        Objects.requireNonNull(description);
        executor.submit(() -> {
            try {
                threadsUsed.incrementAndGet();
                new JobWorker(description).run();
            } finally {
                threadsUsed.decrementAndGet();
            }
        });
    }

}