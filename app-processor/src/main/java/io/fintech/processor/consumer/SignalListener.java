package io.fintech.processor.consumer;

import io.fintech.processor.job.JobDescription;

public interface SignalListener {

    void start();

    void stop();

    void nack(JobDescription jobDescription);

}
