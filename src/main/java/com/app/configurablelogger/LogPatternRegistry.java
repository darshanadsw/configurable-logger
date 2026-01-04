package com.app.configurablelogger;

import com.app.configurablelogger.config.MethodLoggerProperties;
import com.app.configurablelogger.model.LoggingRule;
import com.app.configurablelogger.model.LoggingRuleConfig;
import com.app.configurablelogger.model.RuleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.util.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Registry that manages logging rules and provides rule matching functionality.
 * 
 * <p>This registry maintains a collection of logging rules, each consisting of:
 * <ul>
 *   <li>An AspectJ pointcut expression (compiled from pattern strings)</li>
 *   <li>A merged configuration (rule-specific settings + global defaults)</li>
 * </ul>
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li><b>Rule Management:</b> Stores and manages logging rules loaded from configuration</li>
 *   <li><b>Pattern Conversion:</b> Uses {@link PatternPointcutConverter} to convert pattern strings 
 *       (package, class, method patterns) into AspectJ pointcut expressions</li>
 *   <li><b>Rule Matching:</b> Determines which rule (if any) matches a given method invocation</li>
 *   <li><b>Configuration Reloading:</b> Supports runtime reloading of configuration from Spring Environment</li>
 * </ul>
 *
 * @see PatternPointcutConverter
 * @see LoggingRuleConfig
 * @see MethodLoggerProperties
 */
public class LogPatternRegistry {

    private static final Logger log = LoggerFactory.getLogger(LogPatternRegistry.class);

    private final Environment environment;
    private final PatternPointcutConverter patternConverter;
    private volatile MethodLoggerProperties currentConfig;
    private final List<RuleEntry> ruleEntries = new ArrayList<>();

    public LogPatternRegistry(MethodLoggerProperties defaultConfigs, Environment environment, PatternPointcutConverter patternConverter) {
        this.environment = environment;
        this.patternConverter = patternConverter;
        this.currentConfig = defaultConfigs;
        updateRules(defaultConfigs);
    }

    /**
     * API to trigger a reload of the configuration from the Spring Environment.
     * Call this method if the application properties have changed at runtime.
     */
    public void reload() {
        try {
            Binder binder = Binder.get(environment);
            MethodLoggerProperties newConfig = binder.bind("method-logger", Bindable.of(MethodLoggerProperties.class))
                    .orElseGet(MethodLoggerProperties::new);
            
            this.currentConfig = newConfig;
            updateRules(newConfig);
            log.info("Reloaded logging configuration from Environment.");
        } catch (Exception e) {
            log.error("Failed to reload configuration from environment", e);
        }
    }

    /**
     * Gets the matching rule's configuration for a method, or null if no rule matches.
     * Returns merged config with defaults applied.
     * Rules are matched in order - first matching rule wins.
     */
    public LoggingRuleConfig getMatchingRuleConfig(Method method, Class<?> targetClass) {
        // Find first matching rule (first match wins)
        for (RuleEntry entry : ruleEntries) {
            if (entry.pointcut().matches(method, targetClass) && entry.config().isEnabled()) {
                return entry.config();
            }
        }
        return null; // No matching rule
    }

    /**
     * Gets the current configuration properties.
     */
    public MethodLoggerProperties getConfig() {
        return currentConfig;
    }

    /**
     * Checks if logging is globally enabled.
     * Returns false if method-logger.enabled is set to false, which completely disables all logging.
     */
    public boolean isLoggingEnabled() {
        return currentConfig != null && currentConfig.isEnabled();
    }

    private void updateRules(MethodLoggerProperties config) {
        LoggingRuleConfig defaultConfig = config.getDefaultConfig();
        ruleEntries.clear();
        
        for (LoggingRule rule : config.getRules()) {
            if (StringUtils.hasText(rule.getPattern()) && rule.isEnabled()) {
                AspectJExpressionPointcut pointcut = patternConverter.convert(rule.getPattern());
                LoggingRuleConfig mergedConfig = rule.mergeWithDefaults(defaultConfig);
                ruleEntries.add(new RuleEntry(pointcut, mergedConfig));
            }
        }
        log.debug("Loaded {} logging rules", ruleEntries.size());
    }
}