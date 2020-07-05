# Hydro

## Quick Start

### Step 1 - Add dependency

Add the following to the `repositories` section of your `build.gradle`:

```groovy
maven { url 'https://jitpack.io' }
```

Then add the dependency to the `dependencies` section. Make sure to replace `$version` with the version
you want to use.

```groovy
implementation 'com.github.6Sides:hydro:$version'
```

### Step 2 - Configure sources

Suppose you have a file named `config.yaml` that contains the following data:

```yaml
host: localhost
port: 5432
user:
    username: postgres
    password: password
```

```kotlin
/* Declare Configuration Source */
val config = YAMLConfiguration(FileDataSource(File("config.yaml")))

// Add configuration to Hydro
Hydro.addConfiguration(config)
```

### Step 3 - Use Configuration Values

```kotlin
/*
    Use configuration values. Values are automatically 
    cast to the required type.
*/
class Database {
    val host: String by hydrate("host")
    val port: Int by hydrate("port")
    
    // Nested values are accessed via dots
    val username: Int by hydrate("user.username")
    val password: Int by hydrate("user.password")
}
```

## Advanced Usage

### Using multiple sources

You can combine multiple configuration sources to create the final configuration by using the `overrides` function.

```kotlin
/* Declare Configuration Sources */

// Declare map source
val map = mapOf(
    "host" to "localhost",
    "port" to 5432,
    "user" to mapOf(
        "username" to "postgres",
        "password" to "password"
    )
)

// Declare S3 source
val BUCKET = ... // S3 bucket name
val KEY = ... // S3 object key
val s3 = AmazonS3ClientBuilder.standard()
    .withRegion(Regions.US_EAST_2)
    .withCredentials(DefaultAWSCredentialsProviderChain())
    .build()

/* 
    Create configuration object. Use `overrides` to specify which 
    configurations have the highest priority.
*/
val config =
    PropertiesConfiguration(S3DataSource(s3, BUCKET, KEY)) overrides // Data from remote properties file
    YAMLConfiguration(FileDataSource(File("config.yaml"))) overrides // Data from local yaml file
    MapConfiguration(map) overrides // Data from map declared above
    EnvironmentConfiguration() // Data from environment variables

// Add configuration to Hydro
Hydro.addConfiguration(config)
```

### Namespaces

Namespaces are a way to split up your configuration data into logical groups. Imagine you have these two configuration
files:

```yaml
# postgres.yaml

host: localhost
port: 5432
user:
    username: postgres
    password: password
```

```yaml
# redis.yaml

host: localhost
port: 6379
user:
    username: redis
    password: password
``` 

These two configuration sources have duplicate keys which would cause certain values to be overwritten depending
on the order in which the files were loaded. This can be solved by specifying namespaces like so:

```kotlin
val config =
        // Load with namespace `postgres`
        YAMLConfiguration(FileDataSource("postgres.yaml"), "postgres") overrides
        // Load with namespace `redis`
        YAMLConfiguration(FileDataSource("redis.yaml"), "redis")
```

These values can be accessed a few different ways:

```kotlin
/*
  The `postgres` namespace is specified for each field. This tells Hydro to only look
  for values in that namespace.
*/
class Database {
    val host: String by hydrate("host", "postgres")
    val port: Int by hydrate("port", "postgres")
    
    // Nested values are accessed via dots
    val username: Int by hydrate("user.username", "postgres")
    val password: Int by hydrate("user.password", "postgres")
}
```

```kotlin
/*
  The `postgres` namespace is specified for the class. This tells Hydro to always use that namespace
  for fields in the class.
*/
@HydroNamespace("postgres")
class Database {
    val host: String by hydrate("host")
    val port: Int by hydrate("port")
    
    // Nested values are accessed via dots
    val username: Int by hydrate("user.username")
    val password: Int by hydrate("user.password")
}
```

```kotlin
/* 
  Bind the class to the `postgres` namespace. Same effect as annotating 
  the class with `@HydroNamespace("postgres")`.
*/
Hydro.bindNamespace<Database>("postgres")

class Database {
    val host: String by hydrate("host")
    val port: Int by hydrate("port")
    
    // Nested values are accessed via dots
    val username: Int by hydrate("user.username")
    val password: Int by hydrate("user.password")
}
```

