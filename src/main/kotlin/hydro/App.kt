package hydro

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import hydro.engine.*
import hydro.engine.Hydro.hydrate


fun main(args: Array<String>) {

    val b = mapOf(
        "one" to 11,
        "1" to 10,
        "2" to mapOf<String, Any>(
            "3" to 30
        ),
        "pg_port" to 4000
    )

    val s3 = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.US_EAST_2)
        .withCredentials(DefaultAWSCredentialsProviderChain())
        .build()

    val config =
        PropertiesConfiguration(S3DataSource(s3, "www.dashflight.net-config", "java-postgres/development.properties"), "postgres") overrides
        YAMLConfiguration(FileDataSource("test.yaml")) overrides
        MapConfiguration(b, "postgres")

    Hydro.addConfiguration(config)
    Hydro.addConfiguration(EnvironmentConfiguration())
    Hydro.addConfiguration(YAMLConfiguration(FileDataSource("development.yaml")))


    val test = TestHydrate()
    println(test.value)
    println(test.v)

    println(config)
    println(config.getValue("postgres.one"))
    println(config.getValue("postgres.2"))
    println(config.getValue("nested.key"))
}

class TestHydrate {
    @HydroNamespace("postgres")
    val value: String by hydrate("pg_port")

    val v: ArrayList<*> by hydrate("allowed-headers")
}
