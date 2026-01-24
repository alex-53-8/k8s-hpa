package io.fintech.processor.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class JobWorker {

    private static final Logger logger = LoggerFactory.getLogger(JobWorker.class);

    private final JobDescription description;

    public JobWorker(JobDescription description) {
        this.description = description;
    }

    public void process() {
        logger.debug("Processing a job with ID {}", description.jobId());

        try {
            int min = 1;
            int max = 600;
            int randomNumber = new Random().nextInt(max + 1 - min) + min;

            logger.info("a job {} will be processing {} seconds", description.jobId(), randomNumber);

            Thread.sleep(randomNumber*1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
