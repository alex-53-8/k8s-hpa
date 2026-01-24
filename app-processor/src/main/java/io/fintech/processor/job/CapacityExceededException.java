package io.fintech.processor.job;

public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(String message) {
        super(message);
    }
}
