package com.app.configurablelogger.model;

/**
 * Enum representing different types of pattern expressions.
 */
public enum PatternType {
    ASPECTJ,    // Already an AspectJ expression (execution, within, @)
    PACKAGE,    // Package pattern ending with .*
    METHOD,     // Method pattern: Package.Class.methodName
    CLASS       // Class pattern: Package.Class
}
