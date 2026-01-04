package com.app.configurablelogger;

import com.app.configurablelogger.model.ExecutionResult;
import com.app.configurablelogger.model.LoggingRuleConfig;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Spring AOP interceptor that provides dynamic, configurable method-level logging.
 * 
 * <p>This interceptor is registered via Spring AOP and intercepts method invocations
 * within the configured base package. For each intercepted method, it:
 * <ul>
 *   <li>Checks if the method matches any configured logging rule via {@link LogPatternRegistry}</li>
 *   <li>Logs method invocation details (method name, arguments) before execution</li>
 *   <li>Measures execution time during method execution</li>
 *   <li>Logs completion details (execution time, return value) after execution</li>
 *   <li>Handles exceptions and logs error information</li>
 * </ul>
 * @see LogPatternRegistry
 * @see LoggingRuleConfig
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public class DynamicLoggingInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DynamicLoggingInterceptor.class);
    private final LogPatternRegistry registry;

    public DynamicLoggingInterceptor(LogPatternRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!registry.isLoggingEnabled()) {
            return invocation.proceed();
        }
        
        Method method = invocation.getMethod();
        Class<?> targetClass = getTargetClass(invocation, method);
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, targetClass);
        
        if (shouldSkipLogging(config)) {
            return invocation.proceed();
        }

        String methodName = buildMethodName(targetClass, method);

        logMethodInvocation(methodName, invocation, config);
        
        ExecutionResult executionResult = executeWithTiming(invocation, methodName);
        
        logMethodCompletion(methodName, executionResult, config);
        
        return executionResult.result();
    }

    /**
     * Gets the target class from the method invocation.
     */
    private Class<?> getTargetClass(MethodInvocation invocation, Method method) {
        return invocation.getThis() != null 
            ? AopProxyUtils.ultimateTargetClass(invocation.getThis()) 
            : method.getDeclaringClass();
    }

    /**
     * Checks if logging should be skipped for this method invocation.
     */
    private boolean shouldSkipLogging(LoggingRuleConfig config) {
        return config == null || !config.isEnabled();
    }

    /**
     * Builds the method name string in the format "ClassName.methodName".
     */
    private String buildMethodName(Class<?> targetClass, Method method) {
        return targetClass.getSimpleName() + "." + method.getName();
    }

    /**
     * Logs the method invocation before execution.
     */
    private void logMethodInvocation(String methodName, MethodInvocation invocation, LoggingRuleConfig config) {
        if (config.isLogArguments()) {
            String args = formatArguments(invocation.getArguments(), config.isMaskSensitiveFields());
            log.info(">>> Invoking {} with args: {}", methodName, args);
        } else {
            log.info(">>> Invoking {}", methodName);
        }
    }

    /**
     * Formats method arguments for logging.
     */
    private String formatArguments(Object[] arguments, boolean maskSensitiveFields) {
        return maskSensitiveFields ? "[PROTECTED]" : Arrays.toString(arguments);
    }

    /**
     * Executes the method invocation and measures execution time.
     * Handles exceptions and returns execution result.
     */
    private ExecutionResult executeWithTiming(MethodInvocation invocation, String methodName) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Object result = invocation.proceed();
            return new ExecutionResult(result, stopWatch);
        } catch (Throwable e) {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            log.error("!! Exception in {} after {} ms. Error: {}", methodName, executionTime, e.toString());
            throw e;
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
        }
    }

    /**
     * Logs method completion if execution time threshold is met.
     */
    private void logMethodCompletion(String methodName, ExecutionResult executionResult, LoggingRuleConfig config) {
        long executionTime = executionResult.getExecutionTime();
        
        if (executionTime >= config.getMinExecutionTimeMs()) {
            String resultString = formatReturnValue(executionResult.result(), config);
            log.info("<<< Completed {} in {} ms. Result: {}", methodName, executionTime, resultString);
        }
    }

    /**
     * Formats the return value for logging based on configuration.
     */
    private String formatReturnValue(Object result, LoggingRuleConfig config) {
        if (result == null) {
            return "VOID";
        }
        
        if (!config.isLogReturnValue()) {
            return "[NOT LOGGED]";
        }
        
        if (config.isMaskSensitiveFields()) {
            return "[PROTECTED]";
        }
        
        String resultString = result.toString();
        if (config.getMaxReturnSize() > -1 && resultString.length() > config.getMaxReturnSize()) {
            resultString = resultString.substring(0, config.getMaxReturnSize()) + "... (truncated)";
        }
        
        return resultString;
    }


}