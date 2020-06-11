package hydro.engine

sealed class ConfigValue(
    val key: String
) {
    class SingleConfigValue(
        key: String,
        value: Any
    ): ConfigValue(key)

    class CompositeConfigValue(
        key: String,
        value: List<ConfigValue>
    ): ConfigValue(key)
}
