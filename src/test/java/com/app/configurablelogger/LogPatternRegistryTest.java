package com.app.configurablelogger;

import com.app.configurablelogger.config.MethodLoggerProperties;
import com.app.configurablelogger.model.LoggingRule;
import com.app.configurablelogger.model.LoggingRuleConfig;
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
    private PatternPointcutConverter patternConverter;
    private LogPatternRegistry registry;

    @BeforeEach
    void setUp() {
        defaultProperties = new MethodLoggerProperties();
        defaultProperties.setEnabled(true);
        defaultProperties.setLogArguments(true);
        defaultProperties.setLogReturnValue(true);
        defaultProperties.setRules(new ArrayList<>());
        patternConverter = new PatternPointcutConverter();
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);
    }

    @Test
    void testGetConfig_ReturnsCurrentConfig() {
        MethodLoggerProperties config = registry.getConfig();
        assertThat(config).isNotNull();
        assertThat(config).isSameAs(defaultProperties);
    }

    @Test
    void testPackagePattern_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        assertThat(config).isNotNull();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void testClassPattern_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.TestService");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
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
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
        assertThat(config.isLogArguments()).isFalse();
    }

    @Test
    void testAspectJExpression_CreatesCorrectPointcut() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("execution(* com.example.service..*(..))");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
    }

    @Test
    void testGetMatchingRuleConfig_FirstMatchWins() {
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
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
        assertThat(config.isLogArguments()).isFalse();
    }

    @Test
    void testGetMatchingRuleConfig_NoMatch_ReturnsNull() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.other.package.*");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testGetMatchingRuleConfig_DisabledRule_ReturnsNull() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.*");
        rule.setEnabled(false);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testEmptyPattern_Ignored() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testNullPattern_Ignored() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern(null);
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNull();
    }

    @Test
    void testReload_CallsReloadWithoutException() {
        LoggingRule rule1 = new LoggingRule();
        rule1.setPattern("com.example.service.*");
        rule1.setEnabled(true);
        defaultProperties.getRules().add(rule1);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        registry.reload();

        assertThat(registry.getConfig()).isNotNull();
    }

    @Test
    void testMethodPattern_WithUppercaseMethodName_TreatedAsClassPattern() {
        LoggingRule rule = new LoggingRule();
        rule.setPattern("com.example.service.TestService");
        rule.setEnabled(true);

        defaultProperties.getRules().add(rule);
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
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
        registry = new LogPatternRegistry(defaultProperties, environment, patternConverter);

        Method method = getTestMethod("com.example.service.TestService");
        LoggingRuleConfig config = registry.getMatchingRuleConfig(method, com.example.service.TestService.class);
        
        assertThat(config).isNotNull();
    }

    private Method getTestMethod(String className) {
        try {
            return com.example.service.TestService.class.getDeclaredMethod("testMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

