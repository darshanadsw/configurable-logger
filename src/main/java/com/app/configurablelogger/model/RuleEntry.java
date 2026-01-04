package com.app.configurablelogger.model;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;

/**
 * Rule with their pointcuts and configs
 * @param pointcut
 * @param config
 */
public record RuleEntry(AspectJExpressionPointcut pointcut, LoggingRuleConfig config) {
}
