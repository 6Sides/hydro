package hydro.engine

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType


object Hydro {

    private var config: Configuration? = null
    val namespaces = mutableMapOf<KClass<*>, String>()

    val dataSource get() = config

    fun addConfiguration(
        config: Configuration
    ) {
        if (this.config == null) {
            this.config = config
        } else {
            this.config = config overrides this.config!!
        }
    }

    inline fun <reified T : Any> bindNamespace(namespace: String) {
        namespaces[T::class] = namespace
    }

    fun hydrate(key: String, default: Any? = null): Hydrator {
        require (dataSource != null) { "Hydro is not initialized. Use `Hydro.init()`" }

        return Hydrator(key, default)
    }
}

class Hydrator(val key: String, val default: Any?) {

    inline operator fun <reified R : Any, T : Any> getValue(thisRef: T, property: KProperty<*>): R {
        return cast((getNamespace(thisRef::class, property)).let {
            cache.computeIfAbsent(getKey(it, key)) { _ ->
                val result = computeValue<R, T>(it, thisRef, property)
                result ?: (default ?: error("No value was set for ${getKey(it, key)}"))
            }
        })
    }

    inline fun <reified R : Any, T : Any> computeValue(
        prefix: String,
        thisRef: T,
        property: KProperty<*>
    ): Any? {
        return Hydro.dataSource!!.getValue(getKey(prefix, key))
    }

    fun getKey(prefix: String, key: String): String {
        return if (prefix.isBlank()) {
            key
        } else {
            "${stripDots(prefix)}.${stripDots(key)}"
        }
    }

    private fun stripDots(s: String): String {
        return s.removePrefix(".").removeSuffix(".")
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


fun getNamespace(clazz: KClass<*>, property: KProperty<*>): String {
    return Hydro.namespaces[clazz] ?:
            property.findAnnotation<HydroNamespace>()?.namespace ?:
            clazz.findAnnotation<HydroNamespace>()?.namespace ?: ""
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HydroNamespace(
    val namespace: String
)
