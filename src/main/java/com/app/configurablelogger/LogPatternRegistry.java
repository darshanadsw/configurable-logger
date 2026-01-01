package com.app.configurablelogger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.util.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LogPatternRegistry {

    private final Environment environment;
    private volatile MethodLoggerProperties currentConfig;
    
    // Rules with their pointcuts and configs
    private static class RuleEntry {
        final AspectJExpressionPointcut pointcut;
        final LoggingRule.LoggingRuleConfig config;
        
        RuleEntry(AspectJExpressionPointcut pointcut, LoggingRule.LoggingRuleConfig config) {
            this.pointcut = pointcut;
            this.config = config;
        }
    }
    private final List<RuleEntry> ruleEntries = new ArrayList<>();

    public LogPatternRegistry(MethodLoggerProperties defaultConfigs, Environment environment) {
        this.environment = environment;
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

    private void updateRules(MethodLoggerProperties config) {
        LoggingRule.LoggingRuleConfig defaultConfig = config.getDefaultConfig();
        ruleEntries.clear();
        
        for (LoggingRule rule : config.getRules()) {
            if (StringUtils.hasText(rule.getPattern()) && rule.isEnabled()) {
                AspectJExpressionPointcut pointcut = createPointcut(rule.getPattern());
                LoggingRule.LoggingRuleConfig mergedConfig = rule.mergeWithDefaults(defaultConfig);
                ruleEntries.add(new RuleEntry(pointcut, mergedConfig));
            }
        }
        log.debug("Loaded {} logging rules", ruleEntries.size());
    }

    private AspectJExpressionPointcut createPointcut(String pattern) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expression = pattern;
        if (!pattern.startsWith("execution") && !pattern.startsWith("within") && !pattern.startsWith("@")) {
            expression = "execution(* " + pattern + "..*(..))";
        }
        pointcut.setExpression(expression);
        return pointcut;
    }

    /**
     * Gets the matching rule's configuration for a method, or null if no rule matches.
     * Returns merged config with defaults applied.
     * Rules are matched in order - first matching rule wins.
     */
    public LoggingRule.LoggingRuleConfig getMatchingRuleConfig(Method method, Class<?> targetClass) {
        // Find first matching rule (first match wins)
        for (RuleEntry entry : ruleEntries) {
            if (entry.pointcut.matches(method, targetClass) && entry.config.isEnabled()) {
                return entry.config;
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
}