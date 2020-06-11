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

    Hydro.init(envProvider, TestSource()) {
        overrideValue("Test.KEY", "overridden value!")
    }

    val test = TestHydrate()
    println(test.value)

    val b = mapOf(
        "one" to 11,
        "1" to 10,
        "2" to mapOf<String, Any>(
            "3" to 30
        )
    )

    val config = YAMLConfigurationSource(File("test.yaml")) overrides
        MapConfigurationSource(b) overrides
        TestSource().getConfig(envProvider.getEnvironment())

    println(config)
    println(config.getNested("one"))
    println(config.getNested("2.4"))
    println(config.getNested("nested.key"))
}

@HydroModule("Test")
@HydroPrefix("2")
class TestHydrate {
    val value: String by hydrate("3")
}

class TestSource: ConfigurationDataSource() {

    override fun loadDefaults(environment: String): Map<String, Any> {
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

    override fun loadModule(environment: String, module: String): Map<String, Any>? {
        return mapOf(
            "KEY" to "VALUE",
            "2" to mapOf(
                "3" to "10034"
            ),
            "3" to 564564564
        )
    }
}
