package com.app.configurablelogger.model;

/**
 * Configuration object for a logging rule.
 */
public class LoggingRuleConfig {
    boolean enabled = true;
    boolean logArguments = true;
    boolean logReturnValue = true;
    long minExecutionTimeMs = 0;
    int maxReturnSize = -1;
    boolean maskSensitiveFields = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogArguments() {
        return logArguments;
    }

    public void setLogArguments(boolean logArguments) {
        this.logArguments = logArguments;
    }

    public boolean isLogReturnValue() {
        return logReturnValue;
    }

    public void setLogReturnValue(boolean logReturnValue) {
        this.logReturnValue = logReturnValue;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public void setMinExecutionTimeMs(long minExecutionTimeMs) {
        this.minExecutionTimeMs = minExecutionTimeMs;
    }

    public int getMaxReturnSize() {
        return maxReturnSize;
    }

    public void setMaxReturnSize(int maxReturnSize) {
        this.maxReturnSize = maxReturnSize;
    }

    public boolean isMaskSensitiveFields() {
        return maskSensitiveFields;
    }

    public void setMaskSensitiveFields(boolean maskSensitiveFields) {
        this.maskSensitiveFields = maskSensitiveFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoggingRuleConfig that = (LoggingRuleConfig) o;
        return enabled == that.enabled &&
                logArguments == that.logArguments &&
                logReturnValue == that.logReturnValue &&
                minExecutionTimeMs == that.minExecutionTimeMs &&
                maxReturnSize == that.maxReturnSize &&
                maskSensitiveFields == that.maskSensitiveFields;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, logArguments, logReturnValue, minExecutionTimeMs, maxReturnSize, maskSensitiveFields);
    }
}
