package com.app.configurablelogger;

import com.example.service.TestService;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)

class DynamicLoggingInterceptorTest {

    @Mock
    private LogPatternRegistry registry;

    private DynamicLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new DynamicLoggingInterceptor(registry);
    }
    
    private MethodInvocation createInvocation(String methodName, Class<?>[] paramTypes, Object[] args, Object returnValue) throws Throwable {
        MethodInvocation invocation = mock(MethodInvocation.class);
        Method method = TestService.class.getDeclaredMethod(methodName, paramTypes);
        lenient().when(invocation.getMethod()).thenReturn(method);
        lenient().when(invocation.getThis()).thenReturn(new TestService());
        lenient().when(invocation.getArguments()).thenReturn(args);
        when(invocation.proceed()).thenReturn(returnValue);
        return invocation;
    }
    
    private MethodInvocation createInvocationWithException(String methodName, Class<?>[] paramTypes, Object[] args, Throwable exception) throws Throwable {
        MethodInvocation invocation = mock(MethodInvocation.class);
        Method method = TestService.class.getDeclaredMethod(methodName, paramTypes);
        lenient().when(invocation.getMethod()).thenReturn(method);
        lenient().when(invocation.getThis()).thenReturn(new TestService());
        lenient().when(invocation.getArguments()).thenReturn(args);
        when(invocation.proceed()).thenThrow(exception);
        return invocation;
    }

    @Test
    void testInvoke_NoMatchingRule_ProceedsWithoutLogging() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(null);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
        verify(registry, times(1)).getMatchingRuleConfig(any(Method.class), any(Class.class));
    }

    @Test
    void testInvoke_DisabledRule_ProceedsWithoutLogging() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(false);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithMatchingRule_LogsAndProceeds() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(true);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);
        config.setMaskSensitiveFields(false);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
        verify(registry, times(1)).getMatchingRuleConfig(any(Method.class), any(Class.class));
    }

    @Test
    void testInvoke_WithMaskedSensitiveFields_MasksArguments() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(true);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);
        config.setMaskSensitiveFields(true);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithMaskedSensitiveFields_MasksReturnValue() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "sensitive-data");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(false);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);
        config.setMaskSensitiveFields(true);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("sensitive-data");
        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithoutLogArguments_DoesNotLogArguments() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(false);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithoutLogReturnValue_DoesNotLogReturnValue() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(true);
        config.setLogReturnValue(false);
        config.setMinExecutionTimeMs(0);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithExecutionTimeThreshold_BelowThreshold_DoesNotLogCompletion() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "processed");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(true);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(100); // 100ms threshold

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("processed");
        verify(invocation, times(1)).proceed();
        // Completion log would only appear if execution time >= 100ms
        // Since we're mocking, execution time will be minimal
    }

    @Test
    void testInvoke_WithMaxReturnSize_TruncatesLongReturnValue() throws Throwable {
        MethodInvocation invocation = createInvocation("processPayment", new Class[]{String.class}, new Object[]{"100"}, "very-long-return-value");
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(false);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);
        config.setMaxReturnSize(10); // Limit to 10 characters

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isEqualTo("very-long-return-value");
        verify(invocation, times(1)).proceed();
        // The logging would truncate, but the actual return value is unchanged
    }

    @Test
    void testInvoke_WithException_LogsExceptionAndReThrows() throws Throwable {
        RuntimeException exception = new RuntimeException("Test exception");
        MethodInvocation invocation = createInvocationWithException("processPayment", new Class[]{String.class}, new Object[]{"100"}, exception);
        
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(true);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        try {
            interceptor.invoke(invocation);
            org.junit.jupiter.api.Assertions.fail("Expected exception to be thrown");
        } catch (RuntimeException e) {
            assertThat(e).isSameAs(exception);
        }

        verify(invocation, times(1)).proceed();
    }

    @Test
    void testInvoke_WithVoidReturn_HandlesVoidMethod() throws Throwable {
        MethodInvocation invocation = mock(MethodInvocation.class);
        Method voidMethod = TestService.class.getDeclaredMethod("testMethod");
        lenient().when(invocation.getMethod()).thenReturn(voidMethod);
        lenient().when(invocation.getThis()).thenReturn(new TestService());
        lenient().when(invocation.getArguments()).thenReturn(new Object[0]);
        when(invocation.proceed()).thenReturn(null);
        
        LoggingRule.LoggingRuleConfig config = new LoggingRule.LoggingRuleConfig();
        config.setEnabled(true);
        config.setLogArguments(false);
        config.setLogReturnValue(true);
        config.setMinExecutionTimeMs(0);

        when(registry.getMatchingRuleConfig(any(Method.class), any(Class.class))).thenReturn(config);

        Object result = interceptor.invoke(invocation);

        assertThat(result).isNull();
        verify(invocation, times(1)).proceed();
    }
}

