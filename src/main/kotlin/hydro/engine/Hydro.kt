package hydro.engine

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType


object Hydro {

    private var _dataSource: ConfigurationDataSource? = null
    private var _environmentProvider: EnvironmentProvider? = null

    val dataSource get() = _dataSource

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
        fun overrideValue(key: String, value: Any) {
            overriddenValues[key] = value
        }
    }


    fun hydrate(prefix: String? = null, key: String): Hydrator {
        require (_environmentProvider != null && dataSource != null) { "Hydro is not initialized. Use `Hydro.init()`" }

        return Hydrator(prefix, key)
    }

    fun hydrate(key: String): Hydrator {
        return hydrate(null, key)
    }
}

class Hydrator(val prefix: String?, val key: String) {

    inline operator fun <reified R : Any, T : Any> getValue(thisRef: T, property: KProperty<*>): R {
        return cast((prefix ?: getPrefix(thisRef::class, property)).let {
            cache.computeIfAbsent(getKey(it, key)) { _ ->
                if (Hydro.overriddenValues.containsKey(getKey(it, key))) {
                    Hydro.overriddenValues[getKey(it, key)] as R
                } else {
                    if (getModule(thisRef::class, property).isBlank()) {
                        Hydro.dataSource!!.getConfig(prefix).getNested(getKey(it, key))!!
                    } else {
                        val mod = getModule(thisRef::class, property)
                        Hydro.dataSource!!.getConfig(mod).getNested(getKey(mod, key))!!
                    }
                }
            }
        })
    }

    fun getKey(prefix: String, key: String): String = if (prefix.isBlank()) key else "$prefix.$key"

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


fun getPrefix(clazz: KClass<*>, property: KProperty<*>): String {
    return property.findAnnotation<HydroPrefix>()?.prefix ?:
    clazz.findAnnotation<HydroPrefix>()?.prefix ?: ""
}

fun getModule(clazz: KClass<*>, property: KProperty<*>): String {
    return property.findAnnotation<HydroModule>()?.module ?:
    clazz.findAnnotation<HydroModule>()?.module ?: ""
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HydroModule(
    val module: String = ""
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HydroPrefix(
    val prefix: String = ""
)