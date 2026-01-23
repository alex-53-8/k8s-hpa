package io.fintech.processor.job;

public record JobDescription (
        String name,
        int duration
) {}