# Configurable Logger

A lightweight Spring Boot library that enables dynamic, runtime-configurable logging for method invocations. Define granular logging rules per package, class, or method and update them on the fly without restarting your application.

## Features

*   **Dynamic Configuration**: Update logging rules at runtime without application restart (requires Spring Cloud Config)
*   **Automatic Refresh**: Automatically detects and applies configuration changes when using Spring Cloud Config (enabled by default)
*   **Granular Control**: Define different logging settings for specific packages, classes, or individual methods
*   **Performance Insights**: Log execution time for method calls with configurable thresholds
*   **Data Visibility**: Optionally log method arguments and return values
*   **Safety Mechanisms**: Configure execution time thresholds and mask sensitive data
*   **Zero Code Changes**: Pure configuration-based approach using Spring Boot auto-configuration

## Installation

Add the dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>configurable-logger</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Architecture

### Overview

The library uses Spring AOP (Aspect-Oriented Programming) to intercept method calls and apply configurable logging. The architecture consists of the following components:

1. **Auto-Configuration** (`ConfigurableLoggerAutoConfiguration`)
   - Automatically configures the library when Spring Boot starts
   - Creates AOP proxies for all beans in the specified base package
   - Registers the interceptor and pointcut advisor

2. **Log Pattern Registry** (`LogPatternRegistry`)
   - Manages logging rules and their configurations
   - Converts rule patterns into AspectJ pointcuts
   - Provides rule matching logic for method interception
   - Handles configuration reloading from Spring Environment

3. **Dynamic Logging Interceptor** (`DynamicLoggingInterceptor`)
   - Intercepts method calls within the base package
   - Determines if a method should be logged based on matching rules
   - Executes logging logic with rule-specific configurations

4. **Auto-Refresh Configuration** (`CloudConfigAutoRefreshConfiguration`)
   - Conditionally activated when Spring Cloud Config is present
   - Listens to `EnvironmentChangeEvent` from Spring Cloud Config
   - Automatically reloads configuration when `method-logger` properties change

### How It Works

1. **Initialization Phase**:
   - Spring Boot auto-configuration creates a pointcut advisor that matches all methods in `configurable.logger.base-package`
   - Spring creates AOP proxies for all beans in that package
   - Initial logging rules are loaded from configuration into `LogPatternRegistry`
   - Rules are converted into AspectJ pointcuts for efficient matching

2. **Method Interception**:
   - When a method is called, Spring AOP intercepts it before execution
   - `DynamicLoggingInterceptor` checks if the method matches any logging rule
   - Rules are evaluated in order - the first matching rule wins
   - If a match is found and the rule is enabled, logging proceeds with that rule's configuration

3. **Configuration Reloading** (with Spring Cloud Config):
   - When `POST /actuator/refresh` is called, Spring Cloud Config updates the Environment
   - `EnvironmentChangeEvent` is fired with the changed property keys
   - Auto-refresh listener detects changes to `method-logger` properties
   - `LogPatternRegistry.reload()` reads the new configuration from Environment
   - Rules are updated and pointcuts are regenerated
   - Changes take effect immediately for subsequent method calls

### Component Details

#### LogPatternRegistry

The registry maintains a list of `RuleEntry` objects, each containing:
- **AspectJ Pointcut**: Compiled pattern for efficient matching
- **LoggingRuleConfig**: Merged configuration (rule-specific + defaults)

Key methods:
- `shouldLog(Method, Class<?>)`: Checks if a method matches any rule
- `getMatchingRuleConfig(Method, Class<?>)`: Returns the configuration for the matching rule
- `reload()`: Reloads configuration from Spring Environment

#### DynamicLoggingInterceptor

Implements `MethodInterceptor` to:
- Check if method should be logged via `registry.shouldLog()`
- Retrieve rule-specific configuration via `registry.getMatchingRuleConfig()`
- Log method invocation details (arguments, execution time, return value)
- Apply rule-specific settings (mask sensitive fields, execution time threshold, etc.)

## Configuration

### Basic Setup

In your `application.yml`, configure the base package and logging rules:

```yaml
configurable:
  logger:
    # [Required] The base package where AOP proxies will be created for logging
    base-package: com.mycompany.myapp
    # [Optional] Auto-refresh configuration when Spring Cloud Config changes (default: true)
    # Only applies when Spring Cloud Config is present in the classpath
    auto-refresh: true

method-logger:
  enabled: true  # Global enable/disable flag
  # Default logging configuration (used when rules don't specify values)
  log-arguments: true
  log-return-value: true
  min-execution-time-ms: 0  # Only log if method takes longer than X ms (0 = log everything)
  max-return-size: -1  # Truncate long return values (-1 = no limit)
  mask-sensitive-fields: false  # Mask arguments and return values as [PROTECTED]
  
  # Define rules for specific packages, classes, or methods
  rules:
    - pattern: com.mycompany.myapp.service.*
      log-arguments: true
      log-return-value: true
      min-execution-time-ms: 100  # Only log slow service methods
    
    - pattern: com.mycompany.myapp.controller.*
      log-arguments: false  # Don't log controller arguments
      log-return-value: true
      mask-sensitive-fields: true  # Mask sensitive data
    
    - pattern: com.mycompany.myapp.repository.UserRepository.save
      log-arguments: true
      log-return-value: false  # Don't log return values for save
      min-execution-time-ms: 50
```

### Configuration Properties

#### configurable.logger

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `base-package` | String | **Required** | Base package where AOP proxies will be created |
| `enabled` | Boolean | `true` | Enable/disable the library |
| `auto-refresh` | Boolean | `true` | Enable auto-refresh when Spring Cloud Config is present |

#### method-logger

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | Boolean | `true` | Global enable/disable flag for all logging |
| `log-arguments` | Boolean | `true` | Default: log method arguments |
| `log-return-value` | Boolean | `true` | Default: log return values |
| `min-execution-time-ms` | Long | `0` | Default: minimum execution time to log (0 = log all) |
| `max-return-size` | Integer | `-1` | Default: max return value size (-1 = no limit) |
| `mask-sensitive-fields` | Boolean | `false` | Default: mask sensitive fields as [PROTECTED] |
| `rules` | List\<LoggingRule> | `[]` | List of logging rules (see below) |

#### LoggingRule

Each rule in the `rules` list supports:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `pattern` | String | **Required** | Pattern to match (package, class, or method) |
| `enabled` | Boolean | `true` | Enable/disable this specific rule |
| `log-arguments` | Boolean | null | Override default (null = use default) |
| `log-return-value` | Boolean | null | Override default (null = use default) |
| `min-execution-time-ms` | Long | null | Override default (null = use default) |
| `max-return-size` | Integer | null | Override default (null = use default) |
| `mask-sensitive-fields` | Boolean | null | Override default (null = use default) |

### Rule Matching

Rules are matched using AspectJ pointcut expressions:

1. **Package Patterns**: `com.example.service.*`
   - Matches all classes in the package and subpackages
   - Converted to: `execution(* com.example.service..*(..))`

2. **Class Patterns**: `com.example.controller.OrderController`
   - Matches all methods in the specific class
   - Converted to: `execution(* com.example.controller.OrderController.*(..))`

3. **Method Patterns**: `com.example.repository.UserRepository.save`
   - Matches the specific method
   - Converted to: `execution(* com.example.repository.UserRepository.save(..))`
   - **Detection**: Pattern is treated as a method pattern if:
     - It has at least 2 dots (separating package, class, and method)
     - The last segment (method name) starts with a lowercase letter (Java naming convention)
   - **Note**: If your method name starts with uppercase, use a full AspectJ expression instead

4. **AspectJ Expressions**: `execution(* com.example..*(..))`
   - Supports full AspectJ pointcut expressions
   - Used as-is without conversion
   - Use this for advanced patterns or when automatic detection doesn't work

**Matching Behavior**:
- Rules are evaluated **in order** - first matching rule wins
- If no rule matches, the method is not logged
- Matching happens at runtime for each method call
- The same method signature in different classes can match different rules

### Partial Configuration in Rules

Rules can specify only the properties they want to override. Unspecified properties use the global defaults:

```yaml
method-logger:
  enabled: true
  log-arguments: true  # Default for all rules
  log-return-value: true  # Default for all rules
  min-execution-time-ms: 0  # Default for all rules
  
  rules:
    - pattern: com.example.service.*
      # Only override min-execution-time-ms, others use defaults
      min-execution-time-ms: 100
    
    - pattern: com.example.controller.*
      # Only override log-arguments, others use defaults
      log-arguments: false
```

## Usage Examples

### Example 1: Service Layer Performance Monitoring

Log only slow service methods (>100ms) with full details:

```yaml
method-logger:
  enabled: true
  rules:
    - pattern: com.example.service.*
      log-arguments: true
      log-return-value: true
      min-execution-time-ms: 100  # Only log slow methods
      max-return-size: 500
```

### Example 2: Controller Layer with Sensitive Data Masking

Log controller methods but mask sensitive data:

```yaml
method-logger:
  enabled: true
  rules:
    - pattern: com.example.controller.*
      log-arguments: false  # Don't log arguments
      log-return-value: true
      mask-sensitive-fields: true  # Mask sensitive data
```

### Example 3: Repository Layer - Specific Method

Custom logging for a specific repository method:

```yaml
method-logger:
  enabled: true
  rules:
    - pattern: com.example.repository.UserRepository.save
      log-arguments: true
      log-return-value: false  # Don't log return values for save
      min-execution-time-ms: 50
```

### Example 4: Multiple Layers with Different Settings

```yaml
method-logger:
  enabled: true
  # Default configuration
  log-arguments: true
  log-return-value: true
  min-execution-time-ms: 0
  max-return-size: -1
  mask-sensitive-fields: false
  
  rules:
    # Service layer: Log everything, but only if >100ms
    - pattern: com.example.service.*
      min-execution-time-ms: 100
      max-return-size: 500
    
    # Controller layer: Don't log arguments, mask sensitive data
    - pattern: com.example.controller.*
      log-arguments: false
      mask-sensitive-fields: true
    
    # Repository layer: Log everything (uses defaults)
    - pattern: com.example.repository.*
      # All properties use defaults from above
    
    # Specific method: Custom settings
    - pattern: com.example.service.PaymentService.processPayment
      log-arguments: true
      log-return-value: false
      mask-sensitive-fields: true
      min-execution-time-ms: 0
```

### Example 5: Multiple Packages with Same Settings

Use multiple rules for different packages with the same configuration:

```yaml
method-logger:
  enabled: true
  rules:
    - pattern: com.example.service.*
      log-arguments: true
      log-return-value: true
      min-execution-time-ms: 0
    - pattern: com.example.controller.*
      log-arguments: true
      log-return-value: true
      min-execution-time-ms: 0
```

Or use a broader pattern:

```yaml
method-logger:
  enabled: true
  rules:
    - pattern: com.example..*  # Matches all subpackages
      log-arguments: true
      log-return-value: true
      min-execution-time-ms: 0
```

## Spring Cloud Config Integration

### Dynamic Configuration Updates

For dynamic configuration updates without application restart, use Spring Cloud Config:

1. **Add Spring Cloud Config to your application**:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

2. **Configure Config Server** in `bootstrap.yml`:

```yaml
spring:
  application:
    name: my-application
  cloud:
    config:
      uri: http://config-server:8888
```

3. **Store configuration in Config Server**:

Store your `method-logger` configuration in the Config Server repository (Git, file system, etc.)

4. **Update and Refresh**:

```bash
# Update configuration in Config Server
# Then call refresh endpoint
curl -X POST http://localhost:8080/actuator/refresh

# Library automatically detects changes and reloads configuration
```

### Auto-Refresh Feature

When Spring Cloud Config is present, the library automatically listens to refresh events:

- **Enabled by default** when Spring Cloud Context is on the classpath
- Listens to `EnvironmentChangeEvent` from Spring Cloud Config
- Automatically reloads when `method-logger` properties change
- No manual intervention needed after calling `/actuator/refresh`

**Disable Auto-Refresh**:

```yaml
configurable:
  logger:
    base-package: com.mycompany.myapp
    auto-refresh: false
```

When disabled, configuration changes require an application restart to take effect.

### Technical Details

**How Auto-Refresh Works**:

1. `CloudConfigAutoRefreshConfiguration` is conditionally loaded when:
   - Spring Cloud Context is on the classpath (`@ConditionalOnClass`)
   - `auto-refresh: true` (default)

2. Event listener uses reflection to access `EnvironmentChangeEvent` (since it's optional):
   - Accepts `Object` as parameter
   - Checks class name at runtime
   - Filters events for `method-logger` property changes

3. When changes are detected:
   - `LogPatternRegistry.reload()` is called
   - Configuration is re-read from Spring Environment
   - Rules are regenerated with new pointcuts
   - Changes take effect immediately

**Key Points**:
- Library reads from Spring `Environment`, which Spring Cloud Config updates
- No direct dependency on Config Server - uses standard Spring mechanisms
- Thread-safe reloading using volatile references
- Efficient: only reloads when `method-logger` properties actually change

## Important Notes

### Configuration Location

- **For dynamic updates**: Configuration must be stored externally (Config Server, external files)
- **JAR-packaged config**: Configuration inside the application JAR cannot be changed without repackaging and redeploying
- **Static configuration**: Can be in `application.yml` if you're okay with restarting the application

### Performance Considerations

- Rule matching happens at runtime for each method call
- Pointcuts are compiled and cached for efficient matching
- First matching rule wins - order your rules from specific to general
- Consider using execution time thresholds to reduce log volume

### Best Practices

1. **Order rules from specific to general**: More specific patterns should come first
2. **Use execution time thresholds**: Set `min-execution-time-ms` to log only slow methods in production
3. **Mask sensitive data**: Enable `mask-sensitive-fields` for controllers handling user data
4. **External configuration**: Use Spring Cloud Config for production environments
5. **Test configuration changes**: Verify logging behavior after configuration updates

## Troubleshooting

### Logging Not Appearing

1. Check that `configurable.logger.base-package` is correctly set
2. Verify `method-logger.enabled: true`
3. Ensure at least one rule matches your methods
4. Check that rules have `enabled: true`
5. Verify execution time threshold (`min-execution-time-ms`) if set

### Configuration Not Updating

1. Ensure Spring Cloud Config is properly configured
2. Check that `auto-refresh: true` (default)
3. Verify you're calling `POST /actuator/refresh` after config changes
4. Check application logs for reload messages
5. Verify configuration in Config Server matches expected format

### Rule Not Matching

1. Verify pattern syntax (package, class, or method pattern)
2. Check rule order - first match wins
3. Ensure pattern matches the actual class/method names
4. Try a broader pattern to test matching
5. Check AspectJ pointcut expression if using custom expressions
6. **For method patterns**: Ensure the method name starts with lowercase letter (e.g., `save`, `processPayment`). If your method starts with uppercase, use a full AspectJ expression: `execution(* com.example.Class.MethodName(..))`
