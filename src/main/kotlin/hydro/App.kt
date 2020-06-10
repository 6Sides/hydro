package hydro

import hydro.engine.*
import hydro.engine.Hydro.hydrate


fun main(args: Array<String>) {
    val envProvider = object : EnvironmentProvider {
        override fun getEnvironment(): String {
            return "production"
        }
    }

    Hydro.init(
        envProvider,
        TestSource()
    ) {
        overrideValue("test", "key", "overridden value!")
    }

    val test = TestHydrate()
    println(test.value)
}

@HydroModuleName("test")
class TestHydrate {
    val value: String by hydrate("key")
}

class TestSource: ConfigurationDataSource {
    override fun getValue(environment: String, module: String, key: String): Any {
        return "$environment $module $key"
    }
}
