package com.app.configurablelogger.config;

import com.app.configurablelogger.model.LoggingRule;
import com.app.configurablelogger.model.LoggingRuleConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

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
    public LoggingRuleConfig getDefaultConfig() {
        LoggingRuleConfig config = new LoggingRuleConfig();
        config.setEnabled(enabled);
        config.setLogArguments(logArguments);
        config.setLogReturnValue(logReturnValue);
        config.setMinExecutionTimeMs(minExecutionTimeMs);
        config.setMaxReturnSize(maxReturnSize);
        config.setMaskSensitiveFields(maskSensitiveFields);
        return config;
    }

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

    public List<LoggingRule> getRules() {
        return rules;
    }

    public void setRules(List<LoggingRule> rules) {
        this.rules = rules;
    }
}