package com.app.configurablelogger.model;

import org.springframework.util.StopWatch;

/**
 * Holds the result of method execution along with timing information.
 */
public record ExecutionResult(Object result, StopWatch stopWatch) {

    public long getExecutionTime() {
        return stopWatch.getTotalTimeMillis();
    }
}
