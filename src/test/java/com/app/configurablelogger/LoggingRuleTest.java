package com.app.configurablelogger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingRuleTest {

    @Test
    void testMergeWithDefaults_AllNull_UsesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        // All properties are null

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isTrue(); // From rule
        assertThat(merged.isLogArguments()).isTrue(); // From defaults
        assertThat(merged.isLogReturnValue()).isTrue(); // From defaults
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(100); // From defaults
        assertThat(merged.getMaxReturnSize()).isEqualTo(500); // From defaults
        assertThat(merged.isMaskSensitiveFields()).isTrue(); // From defaults
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

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isFalse(); // From rule
        assertThat(merged.isLogArguments()).isFalse(); // From rule
        assertThat(merged.isLogReturnValue()).isFalse(); // From rule
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(200); // From rule
        assertThat(merged.getMaxReturnSize()).isEqualTo(1000); // From rule
        assertThat(merged.isMaskSensitiveFields()).isFalse(); // From rule
    }

    @Test
    void testMergeWithDefaults_PartialOverride_MixesRuleAndDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setLogArguments(false); // Override
        rule.setMinExecutionTimeMs(300L); // Override
        // logReturnValue, maxReturnSize, maskSensitiveFields are null

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setEnabled(true);
        defaults.setLogArguments(true);
        defaults.setLogReturnValue(true);
        defaults.setMinExecutionTimeMs(100L);
        defaults.setMaxReturnSize(500);
        defaults.setMaskSensitiveFields(true);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isEnabled()).isTrue(); // From rule
        assertThat(merged.isLogArguments()).isFalse(); // From rule (overridden)
        assertThat(merged.isLogReturnValue()).isTrue(); // From defaults
        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(300); // From rule (overridden)
        assertThat(merged.getMaxReturnSize()).isEqualTo(500); // From defaults
        assertThat(merged.isMaskSensitiveFields()).isTrue(); // From defaults
    }

    @Test
    void testMergeWithDefaults_BooleanFalse_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setLogArguments(false); // Explicitly false, not null

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setLogArguments(true);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.isLogArguments()).isFalse(); // False overrides true
    }

    @Test
    void testMergeWithDefaults_LongZero_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setMinExecutionTimeMs(0L); // Explicitly 0, not null

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setMinExecutionTimeMs(100L);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.getMinExecutionTimeMs()).isEqualTo(0); // 0 overrides 100
    }

    @Test
    void testMergeWithDefaults_IntegerNegativeOne_OverridesDefaults() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);
        rule.setMaxReturnSize(-1); // Explicitly -1, not null

        LoggingRule.LoggingRuleConfig defaults = new LoggingRule.LoggingRuleConfig();
        defaults.setMaxReturnSize(500);

        LoggingRule.LoggingRuleConfig merged = rule.mergeWithDefaults(defaults);

        assertThat(merged.getMaxReturnSize()).isEqualTo(-1); // -1 overrides 500
    }
}

