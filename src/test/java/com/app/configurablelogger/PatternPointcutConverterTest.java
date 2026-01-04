package com.app.configurablelogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class PatternPointcutConverterTest {

    private PatternPointcutConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PatternPointcutConverter();
    }

    @Test
    void testConvert_PackagePattern_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.*");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service..*(..))");
    }

    @Test
    void testConvert_ClassPattern_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.UserService");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.UserService.*(..))");
    }

    @Test
    void testConvert_MethodPattern_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.UserService.save");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.UserService.save(..))");
    }

    @Test
    void testConvert_AspectJExpression_UsedAsIs() {
        String aspectjExpression = "execution(* com.example..*(..))";
        AspectJExpressionPointcut pointcut = converter.convert(aspectjExpression);
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo(aspectjExpression);
    }

    @Test
    void testConvert_AspectJExpression_WithWithin_UsedAsIs() {
        String aspectjExpression = "within(com.example.service.*)";
        AspectJExpressionPointcut pointcut = converter.convert(aspectjExpression);
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo(aspectjExpression);
    }

    @Test
    void testConvert_AspectJExpression_WithAnnotation_UsedAsIs() {
        String aspectjExpression = "@annotation(com.example.Logged)";
        AspectJExpressionPointcut pointcut = converter.convert(aspectjExpression);
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo(aspectjExpression);
    }

    @Test
    void testConvert_MethodPattern_WithLowercaseMethodName_DetectedAsMethod() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.UserService.processPayment");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.UserService.processPayment(..))");
    }

    @Test
    void testConvert_MethodPattern_WithMultipleDots_DetectedAsMethod() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.impl.UserServiceImpl.save");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.impl.UserServiceImpl.save(..))");
    }

    @Test
    void testConvert_ClassPattern_WithUppercaseLastSegment_TreatedAsClass() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.UserService.ProcessPayment");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.UserService.ProcessPayment.*(..))");
    }

    @Test
    void testConvert_PackagePattern_WithNestedPackage_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.impl.*");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.impl..*(..))");
    }

    @Test
    void testConvert_ClassPattern_WithNestedPackage_TreatedAsClass() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.impl.UserServiceImpl");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.service.impl.UserServiceImpl.*(..))");
    }

    @Test
    void testConvert_MethodPattern_WithSingleDot_NotDetectedAsMethod() {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.UserService");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* com.example.UserService.*(..))");
    }

    @Test
    void testConvert_PackagePattern_WithDeepNesting_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("a.b.c.d.e.f.*");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* a.b.c.d.e.f..*(..))");
    }

    @Test
    void testConvert_ClassPattern_WithDeepNesting_CreatesCorrectExpression() {
        AspectJExpressionPointcut pointcut = converter.convert("a.b.c.d.e.f.MyClass");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* a.b.c.d.e.f.MyClass.*(..))");
    }

    @Test
    void testConvert_MethodPattern_WithDeepNesting_DetectedAsMethod() {
        AspectJExpressionPointcut pointcut = converter.convert("a.b.c.d.e.f.MyClass.myMethod");
        
        assertThat(pointcut).isNotNull();
        String expression = pointcut.getExpression();
        assertThat(expression).isEqualTo("execution(* a.b.c.d.e.f.MyClass.myMethod(..))");
    }

    @Test
    void testConvert_PackagePattern_PointcutMatchesMethodsInPackage() throws Exception {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.*");
        
        Method method = com.example.service.TestService.class.getDeclaredMethod("testMethod");
        
        assertThat(pointcut.matches(method, com.example.service.TestService.class)).isTrue();
    }

    @Test
    void testConvert_ClassPattern_PointcutMatchesMethodsInClass() throws Exception {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.TestService");
        
        Method method = com.example.service.TestService.class.getDeclaredMethod("testMethod");
        
        assertThat(pointcut.matches(method, com.example.service.TestService.class)).isTrue();
    }

    @Test
    void testConvert_MethodPattern_PointcutMatchesSpecificMethod() throws Exception {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.TestService.testMethod");
        
        Method method = com.example.service.TestService.class.getDeclaredMethod("testMethod");
        
        assertThat(pointcut.matches(method, com.example.service.TestService.class)).isTrue();
    }

    @Test
    void testConvert_MethodPattern_PointcutDoesNotMatchOtherMethod() throws Exception {
        AspectJExpressionPointcut pointcut = converter.convert("com.example.service.TestService.testMethod");
        
        Method method = com.example.service.TestService.class.getDeclaredMethod("processPayment", String.class);
        
        assertThat(pointcut.matches(method, com.example.service.TestService.class)).isFalse();
    }

    @Test
    void testConvert_AspectJExpression_PointcutWorksCorrectly() throws Exception {
        AspectJExpressionPointcut pointcut = converter.convert("execution(* com.example.service..*(..))");
        
        Method method = com.example.service.TestService.class.getDeclaredMethod("testMethod");
        
        assertThat(pointcut.matches(method, com.example.service.TestService.class)).isTrue();
    }
}

