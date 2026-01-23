package io.fintech.processor.rest;

import io.fintech.processor.job.JobDescription;
import io.fintech.processor.job.ProcessorQueueHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/processor")
public class ProcessorController {

    private final ProcessorQueueHandler queueHandler;

    public ProcessorController(ProcessorQueueHandler queueHandler) {
        this.queueHandler = queueHandler;
    }

    public record WorkUnit(
            String name,
            int duration
    ) {}

    @PostMapping
    public void submit(@RequestBody WorkUnit unit) {
        queueHandler.submit(new JobDescription(unit.name, unit.duration));
    }

}

