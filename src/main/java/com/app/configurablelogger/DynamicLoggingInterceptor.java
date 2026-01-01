package com.app.configurablelogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class DynamicLoggingInterceptor implements MethodInterceptor {

    private final LogPatternRegistry registry;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> targetClass = invocation.getThis() != null ? AopProxyUtils.ultimateTargetClass(invocation.getThis()) : method.getDeclaringClass();

        // Get rule-specific configuration for this method
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, targetClass);
        if (config == null || !config.isEnabled()) {
            return invocation.proceed();
        }

        String methodName = targetClass.getSimpleName() + "." + method.getName();

        if (config.isLogArguments()) {
            String args = config.isMaskSensitiveFields() ? "[PROTECTED]" : Arrays.toString(invocation.getArguments());
            log.info(">> Invoking {} with args: {}", methodName, args);
        } else {
            log.info(">> Invoking {}", methodName);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            log.error("!! Exception in {} after {} ms. Error: {}", methodName, executionTime, e.toString());
            throw e; // Re-throw the exception to not alter behavior
        } finally {
            if (stopWatch.isRunning()) stopWatch.stop();
        }

        long executionTime = stopWatch.getTotalTimeMillis();
        if (executionTime >= config.getMinExecutionTimeMs()) {
            String resultString = "VOID";
            if (config.isLogReturnValue() && result != null) {
                if (config.isMaskSensitiveFields()) {
                    resultString = "[PROTECTED]";
                } else {
                    resultString = result.toString();
                    if (config.getMaxReturnSize() > -1 && resultString.length() > config.getMaxReturnSize()) {
                        resultString = resultString.substring(0, config.getMaxReturnSize()) + "... (truncated)";
                    }
                }
            } else if (!config.isLogReturnValue()) {
                resultString = "[NOT LOGGED]";
            }
            log.info("<< Completed {} in {} ms. Result: {}", methodName, executionTime, resultString);
        }

        return result;
    }
}