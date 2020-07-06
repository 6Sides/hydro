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

    Hydro.configure {
        namespace("postgres") {
            PropertiesConfiguration(S3DataSource(s3, "www.dashflight.net-config", "java-postgres/development.properties"))
        }
        namespace("application") {
            YAMLConfiguration(FileDataSource("test.yaml"))
        }
        namespace("env") {
            EnvironmentConfiguration()
        }

        addConfiguration(MapConfiguration(b))

        bindNamespace<TestHydrate>("postgres")
    }

    println(Hydro.dataSource)

    val test = TestHydrate()
    println(test.value)
    println(test.headers)
}

class TestHydrate {
    val value: Int by hydrate("pg_port")

    @HydroNamespace("application")
    val headers: List<String> by hydrate("allowed-headers")
}
