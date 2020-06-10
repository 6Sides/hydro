package hydro.engine

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType


object Hydro {

    private var _dataSource: ConfigurationDataSource? = null
    private var _environmentProvider: EnvironmentProvider? = null

    val dataSource get() = _dataSource
    val environment get() = _environmentProvider?.getEnvironment()

    fun init(
        environmentProvider: EnvironmentProvider,
        dataSource: ConfigurationDataSource,
        overrideValueBlock: (ConfigBlock.() -> Unit)? = null
    ) {
        _environmentProvider = environmentProvider
        _dataSource = dataSource
        overrideValueBlock?.invoke(ConfigBlock)
    }


    val overriddenValues = mutableMapOf<String, Any>()

    object ConfigBlock {
        fun overrideValue(module: String, key: String, value: Any) {
            overriddenValues[module + key] = value
        }
    }


    fun hydrate(module: String? = null, key: String): Hydrator {
        require (_environmentProvider != null && dataSource != null) { "Hydro is not initialized. Use `Hydro.init()`" }

        return Hydrator(module, key)
    }

    fun hydrate(key: String): Hydrator {
        return hydrate(null, key)
    }
}

class Hydrator(val module: String?, val key: String) {

    inline operator fun <reified R : Any, T : Any> getValue(thisRef: T, property: KProperty<*>): R {
        return cast((module ?: getModuleName(thisRef::class, property)).let {
            cache.computeIfAbsent(it + key) { _ ->
                if (Hydro.overriddenValues.containsKey(it+key)) {
                    Hydro.overriddenValues[it+key] as R
                } else {
                    Hydro.dataSource!!.getValue(Hydro.environment!!, it, key)
                }
            }
        })
    }

    inline fun <reified T : Any> cast(value: Any): T {
        if (value::class.starProjectedType == T::class.starProjectedType) {
            return value as T
        }

        return ConverterRegistry().getConverter(value::class.starProjectedType, T::class.starProjectedType).convert(value) as T
    }

    companion object {
        val cache = mutableMapOf<String, Any>()
    }
}


fun getModuleName(clazz: KClass<*>, property: KProperty<*>): String {
    return property.findAnnotation<HydroModuleName>()?.module ?:
    clazz.findAnnotation<HydroModuleName>()?.module ?:
    error("No module declared for property: `$property`")
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HydroModuleName(
    val module: String
)