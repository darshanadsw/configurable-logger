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
    
    /**
     * Enum representing different types of pattern expressions.
     */
    private enum PatternType {
        ASPECTJ,    // Already an AspectJ expression (execution, within, @)
        PACKAGE,    // Package pattern ending with .*
        METHOD,     // Method pattern: Package.Class.methodName
        CLASS       // Class pattern: Package.Class
    }

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
        PatternType type = determinePatternType(pattern);
        
        String expression = switch (type) {
            case ASPECTJ -> pattern;
            case PACKAGE -> convertToPackageExpression(pattern);
            case METHOD -> convertToMethodExpression(pattern);
            case CLASS -> convertToClassExpression(pattern);
        };
        
        pointcut.setExpression(expression);
        return pointcut;
    }
    
    /**
     * Determines the type of pattern based on its structure.
     */
    private PatternType determinePatternType(String pattern) {
        // Check if it's already an AspectJ expression
        if (pattern.startsWith("execution") || pattern.startsWith("within") || pattern.startsWith("@")) {
            return PatternType.ASPECTJ;
        }
        
        // Check if it's a package pattern (ends with .*)
        if (pattern.endsWith(".*")) {
            return PatternType.PACKAGE;
        }
        
        // Check if it's a method pattern
        if (isMethodPattern(pattern)) {
            return PatternType.METHOD;
        }
        
        // Default to class pattern
        return PatternType.CLASS;
    }
    
    /**
     * Checks if a pattern represents a method pattern (Package.Class.methodName).
     * Heuristic: pattern has at least 2 dots and last segment starts with lowercase.
     */
    private boolean isMethodPattern(String pattern) {
        int lastDotIndex = pattern.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex >= pattern.length() - 1) {
            return false;
        }
        
        String lastSegment = pattern.substring(lastDotIndex + 1);
        return lastSegment.length() > 0 
            && Character.isLowerCase(lastSegment.charAt(0))
            && countOccurrences(pattern, '.') >= 2;
    }
    
    /**
     * Converts a package pattern to AspectJ expression.
     * Example: "com.example.service.*" -> "execution(* com.example.service..*(..))"
     */
    private String convertToPackageExpression(String pattern) {
        String packagePattern = pattern.substring(0, pattern.length() - 2);
        return "execution(* " + packagePattern + "..*(..))";
    }
    
    /**
     * Converts a method pattern to AspectJ expression.
     * Example: "com.example.service.MyService.processPayment" -> "execution(* com.example.service.MyService.processPayment(..))"
     */
    private String convertToMethodExpression(String pattern) {
        return "execution(* " + pattern + "(..))";
    }
    
    /**
     * Converts a class pattern to AspectJ expression.
     * Example: "com.example.service.MyService" -> "execution(* com.example.service.MyService.*(..))"
     */
    private String convertToClassExpression(String pattern) {
        return "execution(* " + pattern + ".*(..))";
    }
    
    /**
     * Counts occurrences of a character in a string.
     */
    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
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