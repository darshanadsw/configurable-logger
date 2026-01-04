package com.app.configurablelogger.config;

import com.app.configurablelogger.DynamicLoggingInterceptor;
import com.app.configurablelogger.LogPatternRegistry;
import com.app.configurablelogger.PatternPointcutConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@AutoConfiguration
@EnableConfigurationProperties({ConfigurableLoggerProperties.class, MethodLoggerProperties.class})
@ConditionalOnProperty(prefix = "configurable.logger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConfigurableLoggerAutoConfiguration {

    @Bean
    public PatternPointcutConverter patternPointcutConverter() {
        return new PatternPointcutConverter();
    }

    @Bean
    public LogPatternRegistry logPatternRegistry(MethodLoggerProperties defaultConfigs, Environment environment, PatternPointcutConverter patternConverter) {
        return new LogPatternRegistry(defaultConfigs, environment, patternConverter);
    }

    @Bean
    public DynamicLoggingInterceptor dynamicLoggingInterceptor(LogPatternRegistry registry) {
        return new DynamicLoggingInterceptor(registry);
    }

    @Bean
    public Advisor dynamicLoggingAdvisor(DynamicLoggingInterceptor interceptor, ConfigurableLoggerProperties properties) {
        // We create a broad pointcut that matches everything in the base package.
        // This ensures Spring creates proxies for these beans.
        // The actual decision to log or not is made dynamically inside the Interceptor.
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        
        // Example: execution(* com.example..*(..))
        String expression = String.format("execution(* %s..*(..))", properties.getBasePackage());
        pointcut.setExpression(expression);

        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }

    /**
     * Auto-refresh configuration that listens to Spring Cloud Config refresh events.
     * Only activated when Spring Cloud Context is on the classpath and auto-refresh is enabled.
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.cloud.context.environment.EnvironmentChangeEvent")
    @ConditionalOnProperty(
        prefix = "configurable.logger",
        name = "auto-refresh",
        havingValue = "true",
        matchIfMissing = true
    )
    static class CloudConfigAutoRefreshConfiguration {

        private static final Logger log = LoggerFactory.getLogger(CloudConfigAutoRefreshConfiguration.class);

        private final LogPatternRegistry registry;

        public CloudConfigAutoRefreshConfiguration(LogPatternRegistry registry) {
            this.registry = registry;
        }

        /**
         * Listens to Spring Cloud Config refresh events and automatically reloads
         * the method logger configuration when method-logger properties change.
         */
        @EventListener
        public void onEnvironmentChange(Object event) {
            // Use reflection to check if this is an EnvironmentChangeEvent
            // and get the keys without importing the class (since it's optional)
            try {
                Class<?> eventClass = event.getClass();
                if ("org.springframework.cloud.context.environment.EnvironmentChangeEvent".equals(eventClass.getName())) {
                    // Get the keys from the event using reflection
                    java.lang.reflect.Method getKeysMethod = eventClass.getMethod("getKeys");
                    @SuppressWarnings("unchecked")
                    java.util.Collection<String> keys = (java.util.Collection<String>) getKeysMethod.invoke(event);
                    
                    // Check if any method-logger properties changed
                    boolean shouldReload = keys.stream().anyMatch(key -> key.startsWith("method-logger"));
                    
                    if (shouldReload) {
                        log.info("Spring Cloud Config refresh detected changes to method-logger properties. Auto-reloading configuration...");
                        registry.reload();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to process environment change event", e);
            }
        }
    }
}