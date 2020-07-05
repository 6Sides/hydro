# Hydro

## Quick Start

### Step 1 - Add dependency

Add the following to the `repositories` section of your `build.gradle`:

```groovy
maven { url 'https://jitpack.io' }
```

Then add the dependency to the `dependencies` section. Make sure to replace `$version` with the version
you want to use!

```groovy
implementation 'com.github.6Sides:hydro:$version'
```

### Step 2 - Configure sources

```kotlin
    /* Declare Configuration Sources */
    
    // Declare map source
    val map = mapOf(
        "one" to 1,
        "two" to mapOf<String, Any>(
            "three" to 3
        ),
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
        configurations have the highest priority
    */
    val config =
        PropertiesConfiguration(S3DataSource(s3, BUCKET, KEY)) overrides // Data from remote properties file
        YAMLConfiguration(FileDataSource(File("config.yaml"))) overrides // Data from local yaml file
        MapConfiguration(map) overrides // Data from map declared above
        EnvironmentConfiguration() // Data from environment variables
    
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
}
```