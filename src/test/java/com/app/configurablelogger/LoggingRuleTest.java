package com.app.configurablelogger;

import com.app.configurablelogger.model.LoggingRule;
import com.app.configurablelogger.model.LoggingRuleConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingRuleTest {

    @Test
    void testMergeWithDefaults_AllNull_UsesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isTrue();
        assertThat(merged.isLogArguments()).isTrue();
        assertThat(merged.isLogReturnValue()).isTrue();
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(100);
        assertThat(merged.getMaxReturnSize()).isEqualTo(500);
        assertThat(merged.isMaskSensitiveFields()).isTrue();
    }

    @Test
    void testMergeWithDefaults_AllOverridden_UsesRuleValues() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(false);
        rule.setLogArguments(false);
        rule.setLogReturnValue(false);
        rule.setMinExecutionTimeMs(200L);
        rule.setMaxReturnSize(1000);
        rule.setMaskSensitiveFields(false);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isFalse();
        assertThat(merged.isLogArguments()).isFalse();
        assertThat(merged.isLogReturnValue()).isFalse();
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(200);
        assertThat(merged.getMaxReturnSize()).isEqualTo(1000);
        assertThat(merged.isMaskSensitiveFields()).isFalse();
    }

    @Test
    void testMergeWithDefaults_PartialOverride_MixesRuleAndDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setLogArguments(false);
        rule.setMinExecutionTimeMs(300L);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isTrue();
        assertThat(merged.isLogArguments()).isFalse();
        assertThat(merged.isLogReturnValue()).isTrue();
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(300);
        assertThat(merged.getMaxReturnSize()).isEqualTo(500);
        assertThat(merged.isMaskSensitiveFields()).isTrue();
    }

    @Test
    void testMergeWithDefaults_BooleanFalse_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setLogArguments(false);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setLogArguments(true);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isLogArguments()).isFalse();
    }

    @Test
    void testMergeWithDefaults_LongZero_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setMinExecutionTimeMs(0L);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setMinExecutionTimeMs(100L);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(0);
    }

    @Test
    void testMergeWithDefaults_IntegerNegativeOne_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setMaxReturnSize(-1);

        LoggingRuleConfig defaults = new LoggingRuleConfig();
        defaults.setMaxReturnSize(500);

        LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.getMaxReturnSize()).isEqualTo(-1);
    }
}

