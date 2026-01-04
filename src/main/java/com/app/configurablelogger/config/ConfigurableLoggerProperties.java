package com.app.configurablelogger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "configurable.logger")
public class ConfigurableLoggerProperties {

    /**
     * The base package to scan for potential logging candidates.
     * Proxies will be created for beans in this package.
     */
    private String basePackage = "com.example";

    /**
     * Whether to automatically reload configuration when Spring Cloud Config refresh events occur.
     * Only applies when Spring Cloud Config is present in the classpath.
     * Defaults to true if Spring Cloud Config is available.
     */
    private boolean autoRefresh = true;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
}