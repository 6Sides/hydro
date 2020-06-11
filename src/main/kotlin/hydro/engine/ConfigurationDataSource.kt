package hydro.engine

/**
 * Fetches configuration data from a source.
 */
abstract class ConfigurationDataSource {

    open fun loadDefaults(environment: String): Map<String, Any> {
        error("Default configuration loading isn't enabled! Override `ConfigurationDataSource.loadDefaults()`")
    }

    open fun loadModule(environment: String, module: String): Map<String, Any>? {
        error("Module loading isn't enabled! Override `ConfigurationDataSource.loadModule()`")
    }

    private val cache = mutableMapOf<String, Map<String, Any>>()

    fun getConfig(environment: String, module: String? = null): ConfigData {
        val res = if (module == null) {
            this.loadDefaults(environment)
        } else {
            val mod = cache.computeIfAbsent(environment) { env ->
                this.loadModule(
                    env,
                    module
                ) ?: error("Failed to load configuration module `$module`")
            }

            mapOf(module to
                    mod
            )
        }

        return MapConfigurationSource(res)
    }
}