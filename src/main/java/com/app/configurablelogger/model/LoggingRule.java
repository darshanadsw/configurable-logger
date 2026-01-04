package com.app.configurablelogger.model;

/**
 * Represents a logging rule with a pattern (package/class/method) and its specific configuration.
 */
public class LoggingRule {
    /**
     * Pattern to match (package, class, or method).
     * Examples:
     * - com.example.service.*
     * - com.example.controller.OrderController
     * - com.example.repository.UserRepository.save
     */
    private String pattern;
    
    /**
     * Whether this rule is enabled.
     */
    private boolean enabled = true;
    
    /**
     * Whether to log method arguments.
     */
    private Boolean logArguments;
    
    /**
     * Whether to log return values.
     */
    private Boolean logReturnValue;
    
    /**
     * Minimum execution time in milliseconds to log (0 = log everything).
     */
    private Long minExecutionTimeMs;
    
    /**
     * Maximum return value size (-1 = no limit).
     */
    private Integer maxReturnSize;
    
    /**
     * Whether to mask sensitive fields.
     */
    private Boolean maskSensitiveFields;
    
    /**
     * Merges this rule's configuration with default values.
     * Null values in the rule are filled from defaults.
     */
    public LoggingRuleConfig mergeWithDefaults(LoggingRuleConfig defaults) {
        LoggingRuleConfig config = new LoggingRuleConfig();
        config.setEnabled(enabled);
        config.setLogArguments(logArguments != null ? logArguments : defaults.isLogArguments());
        config.setLogReturnValue(logReturnValue != null ? logReturnValue : defaults.isLogReturnValue());
        config.setMinExecutionTimeMs(minExecutionTimeMs != null ? minExecutionTimeMs : defaults.getMinExecutionTimeMs());
        config.setMaxReturnSize(maxReturnSize != null ? maxReturnSize : defaults.getMaxReturnSize());
        config.setMaskSensitiveFields(maskSensitiveFields != null ? maskSensitiveFields : defaults.isMaskSensitiveFields());
        return config;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getLogArguments() {
        return logArguments;
    }

    public void setLogArguments(Boolean logArguments) {
        this.logArguments = logArguments;
    }

    public Boolean getLogReturnValue() {
        return logReturnValue;
    }

    public void setLogReturnValue(Boolean logReturnValue) {
        this.logReturnValue = logReturnValue;
    }

    public Long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public void setMinExecutionTimeMs(Long minExecutionTimeMs) {
        this.minExecutionTimeMs = minExecutionTimeMs;
    }

    public Integer getMaxReturnSize() {
        return maxReturnSize;
    }

    public void setMaxReturnSize(Integer maxReturnSize) {
        this.maxReturnSize = maxReturnSize;
    }

    public Boolean getMaskSensitiveFields() {
        return maskSensitiveFields;
    }

    public void setMaskSensitiveFields(Boolean maskSensitiveFields) {
        this.maskSensitiveFields = maskSensitiveFields;
    }
}

