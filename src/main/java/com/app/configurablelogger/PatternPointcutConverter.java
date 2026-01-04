package com.app.configurablelogger;

import com.app.configurablelogger.model.PatternType;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

/**
 * Service responsible for converting pattern strings into AspectJ pointcut expressions.
 * 
 * <p>This converter supports multiple pattern formats:
 * <ul>
 *   <li><b>Package Patterns:</b> {@code com.example.service.*}
 *       <br>Matches all classes in the package and subpackages
 *       <br>Converted to: {@code execution(* com.example.service..*(..))}</li>
 *       
 *   <li><b>Class Patterns:</b> {@code com.example.service.MyService}
 *       <br>Matches all methods in the specific class
 *       <br>Converted to: {@code execution(* com.example.service.MyService.*(..))}</li>
 *       
 *   <li><b>Method Patterns:</b> {@code com.example.service.MyService.processPayment}
 *       <br>Matches the specific method
 *       <br>Converted to: {@code execution(* com.example.service.MyService.processPayment(..))}
 *       <br><b>Detection:</b> Pattern is treated as a method if it has at least 2 dots 
 *       and the last segment (method name) starts with a lowercase letter (Java naming convention)</li>
 *       
 *   <li><b>AspectJ Expressions:</b> {@code execution(* com.example..*(..))}
 *       <br>Full AspectJ pointcut expressions (starting with "execution", "within", or "@")
 *       <br>Used as-is without conversion</li>
 * </ul>
 * 
 * <p><b>Pattern Detection Logic:</b>
 * The converter uses heuristics to determine the pattern type:
 * <ol>
 *   <li>If pattern starts with "execution", "within", or "@" → AspectJ expression (used as-is)</li>
 *   <li>Else if pattern ends with ".*" → Package pattern</li>
 *   <li>Else if pattern has ≥2 dots and last segment starts with lowercase → Method pattern</li>
 *   <li>Else → Class pattern (default)</li>
 * </ol>
 * @see LogPatternRegistry
 * @see PatternType
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
public class PatternPointcutConverter {

    /**
     * Converts a pattern string into an AspectJ expression pointcut.
     * 
     * @param pattern The pattern to convert (package, class, method, or AspectJ expression)
     * @return AspectJExpressionPointcut configured with the appropriate expression
     */
    public AspectJExpressionPointcut convert(String pattern) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        PatternType type = determinePatternType(pattern);
        
        String expression = switch (type) {
            case ASPECTJ -> pattern;
            case PACKAGE -> convertToPackageExpression(pattern);
            case METHOD -> convertToMethodExpression(pattern);
            case CLASS -> convertToClassExpression(pattern);
        };
        
        pointcut.setExpression(expression);
        return pointcut;
    }
    
    /**
     * Determines the type of pattern based on its structure.
     */
    private PatternType determinePatternType(String pattern) {
        // Check if it's already an AspectJ expression
        if (pattern.startsWith("execution") || pattern.startsWith("within") || pattern.startsWith("@")) {
            return PatternType.ASPECTJ;
        }
        
        // Check if it's a package pattern (ends with .*)
        if (pattern.endsWith(".*")) {
            return PatternType.PACKAGE;
        }
        
        // Check if it's a method pattern
        if (isMethodPattern(pattern)) {
            return PatternType.METHOD;
        }
        
        // Default to class pattern
        return PatternType.CLASS;
    }
    
    /**
     * Checks if a pattern represents a method pattern (Package.Class.methodName).
     * Heuristic: pattern has at least 2 dots and last segment starts with lowercase.
     */
    private boolean isMethodPattern(String pattern) {
        int lastDotIndex = pattern.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex >= pattern.length() - 1) {
            return false;
        }
        
        String lastSegment = pattern.substring(lastDotIndex + 1);
        return !lastSegment.isEmpty()
            && Character.isLowerCase(lastSegment.charAt(0))
            && countOccurrences(pattern, '.') >= 2;
    }
    
    /**
     * Converts a package pattern to AspectJ expression.
     * Example: "com.example.service.*" -> "execution(* com.example.service..*(..))"
     */
    private String convertToPackageExpression(String pattern) {
        String packagePattern = pattern.substring(0, pattern.length() - 2);
        return "execution(* " + packagePattern + "..*(..))";
    }
    
    /**
     * Converts a method pattern to AspectJ expression.
     * Example: "com.example.service.MyService.processPayment" -> "execution(* com.example.service.MyService.processPayment(..))"
     */
    private String convertToMethodExpression(String pattern) {
        return "execution(* " + pattern + "(..))";
    }
    
    /**
     * Converts a class pattern to AspectJ expression.
     * Example: "com.example.service.MyService" -> "execution(* com.example.service.MyService.*(..))"
     */
    private String convertToClassExpression(String pattern) {
        return "execution(* " + pattern + ".*(..))";
    }
    
    /**
     * Counts occurrences of a character in a string.
     */
    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
}

