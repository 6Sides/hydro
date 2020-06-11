package hydro

import hydro.engine.*
import hydro.engine.Hydro.hydrate
import java.io.File


fun main(args: Array<String>) {
    val envProvider = object : EnvironmentProvider {
        override fun getEnvironment(): String {
            return "production"
        }
    }

    Hydro.init(envProvider, TestSource(envProvider)) {
        overrideValue("key", "overridden value!")
    }

    val test = TestHydrate()
    println(test.value)

    /*val a = mapOf(
        "1" to 1,
        "2" to mapOf<String, Any>(
            "3" to 3,
            "4" to 4
        )
    )*/

    val b = mapOf(
        "one" to 11,
        "1" to 10,
        "2" to mapOf<String, Any>(
            "3" to 30
        )
    )

    val config = PropertiesConfigurationSource(File("test.properties")) overrides
        MapConfigurationSource(b) overrides
        TestSource(envProvider).getConfig()

    println(config)
    println(config.getNested("one"))
    println(config.getNested("2.4"))
}

@HydroModule("TESFasdf")
class TestHydrate {
    val value: String by hydrate("KEY")
}

class TestSource(
    environmentProvider: EnvironmentProvider
): ConfigurationDataSource(environmentProvider) {
    override fun load(environment: String): Map<String, Any> {
        return mapOf(
            "KEY" to "VALUE!!!",
            "key" to "value!",
            "1" to 1,
            "2" to mapOf<String, Any>(
                "3" to 3,
                "4" to 4
            )
        )
    }

    override fun loadModule(environment: String, module: String): Map<String, Any> {
        return mapOf(
            "KEY" to "VALUE"
        )
    }
}
