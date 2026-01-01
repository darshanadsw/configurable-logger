package com.app.configurablelogger;

import lombok.Data;

/**
 * Represents a logging rule with a pattern (package/class/method) and its specific configuration.
 */
@Data
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
    
    /**
     * Configuration object for a logging rule.
     */
    @Data
    public static class LoggingRuleConfig {
        private boolean enabled = true;
        private boolean logArguments = true;
        private boolean logReturnValue = true;
        private long minExecutionTimeMs = 0;
        private int maxReturnSize = -1;
        private boolean maskSensitiveFields = false;
    }
}

