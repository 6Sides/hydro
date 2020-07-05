package hydro.engine

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType


object Hydro {

    private var _dataSource: Configuration? = null

    val dataSource get() = _dataSource

    fun init(
        dataSource: Configuration
    ) {
        _dataSource = dataSource
    }


    fun hydrate(key: String): Hydrator {
        require (dataSource != null) { "Hydro is not initialized. Use `Hydro.init()`" }

        return Hydrator(null, key)
    }
}

class Hydrator(val prefix: String?, val key: String) {

    inline operator fun <reified R : Any, T : Any> getValue(thisRef: T, property: KProperty<*>): R {
        return cast((prefix ?: getNamespace(thisRef::class, property)).let {
            cache.computeIfAbsent(getKey(it, key)) { _ ->
                val result = computeValue<R, T>(it, thisRef, property)
                result ?: error("No value was set for ${getKey(it, key)}")
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
    return property.findAnnotation<HydroNamespace>()?.namespace ?:
    clazz.findAnnotation<HydroNamespace>()?.namespace ?: ""
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HydroNamespace(
    val namespace: String
)
