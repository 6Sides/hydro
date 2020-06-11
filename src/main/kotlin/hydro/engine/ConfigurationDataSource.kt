package hydro.engine

/**
 * Fetches configuration data from a source.
 */
abstract class ConfigurationDataSource(
    private val environmentProvider: EnvironmentProvider
): ConfigData() {

    override val data: Map<String, Any>
        get() = getConfig()

    abstract fun load(environment: String): Map<String, Any>

    open fun loadModule(environment: String, module: String): Map<String, Any> {
        return load(environment)[module] as Map<String, Any>
    }

    fun getConfig(module: String? = null): ConfigData {
        val res = if (module == null) {
            this.load(
                environmentProvider.getEnvironment()
            )
        } else {
            mapOf(module to
            this.loadModule(
                environmentProvider.getEnvironment(),
                module
            )
            )
        }

        return MapConfigurationSource(res)
    }

}