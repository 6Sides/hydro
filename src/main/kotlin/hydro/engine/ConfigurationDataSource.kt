package hydro.engine

/**
 * Fetches configuration data from a source.
 */
interface ConfigurationDataSource {

    fun getValue(environment: String, module: String, key: String): Any

}