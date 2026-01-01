package com.app.configurablelogger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "method-logger")
public class MethodLoggerProperties {
    /**
     * Global enable/disable flag.
     * When false, all logging is disabled regardless of rules.
     */
    private boolean enabled = true;
    
    /**
     * Default logging configuration.
     * Used as defaults when rules don't specify values.
     */
    private boolean logArguments = true;
    private boolean logReturnValue = true;
    private long minExecutionTimeMs = 0;
    private int maxReturnSize = -1;
    private boolean maskSensitiveFields = false;
    
    /**
     * List of logging rules with specific configurations.
     * Each rule defines a pattern and its own configuration.
     */
    private List<LoggingRule> rules = new ArrayList<>();
    
    /**
     * Default configuration used when merging rule-specific configs.
     */
    public LoggingRule.LoggingRuleConfig getDefaultConfig() {
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(enabled);
        config.setLogArguments(logArguments);
        config.setLogReturnValue(logReturnValue);
        config.setMinExecutionTimeMs(minExecutionTimeMs);
        config.setMaxReturnSize(maxReturnSize);
        config.setMaskSensitiveFields(maskSensitiveFields);
        return config;
    }
}