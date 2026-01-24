package io.fintech.processor.job;

import java.util.UUID;

public record JobDescription (
        UUID jobId,
        String name
) {}