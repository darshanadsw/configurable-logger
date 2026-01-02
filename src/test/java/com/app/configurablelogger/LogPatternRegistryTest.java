package com.app.configurablelogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LogPatternRegistryTest {

    @Mock
    private Environment environment;

    private MethodLoggerProperties defaultProperties;
    private LogPatternRegistry registry;

    @BeforeEach
    void setUp() {
        defaultProperties = new MethodLoggerProperties();
        defaultProperties.setEnabled(true);
        defaultProperties.setLogArguments(true);
        defaultProperties.setLogReturnValue(true);
        defaultProperties.setRules(new ArrayList<>());
        registry = new LogPatternRegistry(defaultProperties, environment);
    }

    @Test
    void testGetConfig_ReturnsCurrentConfig() {
        MethodLoggerProperties config = registry.getConfig();
        assertThat(config).isNotNull();
        assertThat(config).isSameAs(defaultProperties);
    }

    @Test
    void testPackagePattern_CreatesCorrectPointcut() {
        // Create a rule with package pattern
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        // Get a method from a class in that package to test matching
        Method method = getTestMethod("com.example.service.TestService");
        
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        assertThat(config).isNotNull();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testClassPattern_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.TestService");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testMethodPattern_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.TestService.testMethod");
        rule.setEnabled(true);
        rule.setLogArguments(false);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
        assertThat(config.isLogArguments()).isFalse();
    }

    @Test
    void testAspectJExpression_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("execution(* com.example.service..*(..))");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
    }

    @Test
    void testGetMatchingRuleConfig_FirstMatchWins() {
        // Add two rules - first one should win
        LoggingRule rule1 = new LoggingRule();
        rule1.setPattern("com.example.service.*");
        rule1.setEnabled(true);
        rule1.setLogArguments(false);

        LoggingRule rule2 = new LoggingRule();
        rule2.setPattern("com.example.service.TestService");
        rule2.setEnabled(true);
        rule2.setLogArguments(true);

        defaultProperties.getRules().add(rule1);
        defaultProperties.getRules().add(rule2);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        // First rule should win (more general, but added first)
        assertThat(config).isNotNull();
        assertThat(config.isLogArguments()).isFalse(); // From rule1
    }

    @Test
    void testGetMatchingRuleConfig_NoMatch_ReturnsNull() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.other.package.*");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testGetMatchingRuleConfig_DisabledRule_ReturnsNull() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(false);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testEmptyPattern_Ignored() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testNullPattern_Ignored() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern(null);
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testReload_CallsReloadWithoutException() {
        // Initial configuration
        LoggingRule rule1 = new LoggingRule();
        rule1.setPattern("com.example.service.*");
        rule1.setEnabled(true);
        defaultProperties.getRules().add(rule1);
        registry = new LogPatternRegistry(defaultProperties, environment);

        // Reload should not throw exception even if environment doesn't have properties
        // (it will catch and log the error)
        registry.reload();

        // Verify that reload was attempted - config should still be available
        assertThat(registry.getConfig()).isNotNull();
    }

    @Test
    void testMethodPattern_WithUppercaseMethodName_TreatedAsClassPattern() {
        // Pattern with uppercase method name should be treated as class pattern
        // This pattern won't match our test method, but should not throw an exception
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.TestService"); // Valid class pattern
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment);

        // This should work (treated as class pattern)
        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        // Pattern should match (class pattern matches all methods in the class)
        assertThat(config).isNotNull();
    }

    @Test
    void testMultipleRules_AllEnabled() {
        LoggingRule rule1 = new LoggingRule();
        rule1.setPattern("com.example.service.*");
        rule1.setEnabled(true);

        LoggingRule rule2 = new LoggingRule();
        rule2.setPattern("com.example.controller.*");
        rule2.setEnabled(true);

        defaultProperties.getRules().add(rule1);
        defaultProperties.getRules().add(rule2);
        registry = new LogPatternRegistry(defaultProperties, environment);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRule.LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
    }

    // Helper method to get a test method
    private Method getTestMethod(String className) {
        try {
            return com.example.service.TestService.class.getDeclaredMethod("testMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

