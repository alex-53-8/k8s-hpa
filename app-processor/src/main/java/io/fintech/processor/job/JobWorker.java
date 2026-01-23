package io.fintech.processor.job;

public class JobWorker implements Runnable {

    private final JobDescription jobDescription;

    public JobWorker(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(jobDescription.duration()*1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
