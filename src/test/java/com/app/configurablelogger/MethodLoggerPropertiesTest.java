package com.app.configurablelogger;

import com.app.configurablelogger.config.MethodLoggerProperties;
import com.app.configurablelogger.model.LoggingRule;
import com.app.configurablelogger.model.LoggingRuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodLoggerPropertiesTest {

    private MethodLoggerProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MethodLoggerProperties();
    }

    @Test
    void testGetDefaultConfig_WithDefaultValues() {
        properties.setEnabled(true);
        properties.setLogArguments(true);
        properties.setLogReturnValue(true);
        properties.setMinExecutionTimeMs(0);
        properties.setMaxReturnSize(-1);
        properties.setMaskSensitiveFields(false);

        LoggingRuleConfig config = properties.getDefaultConfig();

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isLogArguments()).isTrue();
        assertThat(config.isLogReturnValue()).isTrue();
        assertThat(config.getMinExecutionTimeMs()).isEqualTo(0);
        assertThat(config.getMaxReturnSize()).isEqualTo(-1);
        assertThat(config.isMaskSensitiveFields()).isFalse();
    }

    @Test
    void testGetDefaultConfig_WithCustomValues() {
        properties.setEnabled(false);
        properties.setLogArguments(false);
        properties.setLogReturnValue(false);
        properties.setMinExecutionTimeMs(100);
        properties.setMaxReturnSize(500);
        properties.setMaskSensitiveFields(true);

        LoggingRuleConfig config = properties.getDefaultConfig();

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isLogArguments()).isFalse();
        assertThat(config.isLogReturnValue()).isFalse();
        assertThat(config.getMinExecutionTimeMs()).isEqualTo(100);
        assertThat(config.getMaxReturnSize()).isEqualTo(500);
        assertThat(config.isMaskSensitiveFields()).isTrue();
    }

    @Test
    void testGetDefaultConfig_ReturnsNewInstance() {
        LoggingRuleConfig config1 = properties.getDefaultConfig();
        LoggingRuleConfig config2 = properties.getDefaultConfig();

        assertThat(config1).isNotSameAs(config2);
        assertThat(config1).isEqualTo(config2);
    }

    @Test
    void testRules_InitiallyEmpty() {
        assertThat(properties.getRules()).isNotNull();
        assertThat(properties.getRules()).isEmpty();
    }

    @Test
    void testRules_CanAddRules() {
        LoggingRule rule1 = new LoggingRule();
        rule1.setPattern("com.example.service.*");

        LoggingRule rule2 = new LoggingRule();
        rule2.setPattern("com.example.controller.*");

        properties.getRules().add(rule1);
        properties.getRules().add(rule2);

        assertThat(properties.getRules()).hasSize(2);
        assertThat(properties.getRules().get(0).getPattern()).isEqualTo("com.example.service.*");
        assertThat(properties.getRules().get(1).getPattern()).isEqualTo("com.example.controller.*");
    }
}

