package hydro.engine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.IOException
import java.util.*

abstract class Configuration(
    protected val namespace: String? = null
) {

    abstract fun getData(): Map<String, Any>

    fun getValue(key: String): Any? {
        val parts = key.split(".")
        var d = getData()

        var res: Any? = null

        for (part in parts) {
            if (part in d) {
                res = d[part]

                if (d[part] is Map<*, *>) {
                    d = d[part] as Map<String, Any>
                }
            } else {
                res = null
                break
            }
        }

        return res
    }

    override fun toString(): String {
        return getData().toString()
    }
}

class MapConfiguration(
    private val data: Map<String, Any>,
    namespace: String? = null
) : Configuration(namespace) {

    override fun getData(): Map<String, Any> {
        return if (namespace == null) {
            data
        } else {
            mapOf(namespace to data)
        }
    }
}

class YAMLConfiguration(
    source: HydroDataSource,
    namespace: String? = null
) : Configuration(namespace) {

    private val data: Map<String, Any> = try {
        ObjectMapper(YAMLFactory()).registerModule(KotlinModule()).readValue(source.load(), object: TypeReference<HashMap<String, Any>>(){})
    } catch (e: IOException) {
        e.printStackTrace()
        emptyMap()
    }

    override fun getData(): Map<String, Any> {
        return if (namespace == null) {
            data
        } else {
            mapOf(namespace to data)
        }
    }
}

class PropertiesConfiguration(
    source: HydroDataSource,
    namespace: String? = null
) : Configuration(namespace) {

    private val data: Map<String, Any> = try {
        JavaPropsMapper().readValue(source.load(), Properties::class.java)
    } catch (e: IOException) {
        e.printStackTrace()
        Properties()
    } as Map<String, Any>

    override fun getData(): Map<String, Any> {
        return if (namespace == null) {
            data
        } else {
            mapOf(namespace to data)
        }
    }
}

class EnvironmentConfiguration(namespace: String? = null) : Configuration(namespace) {

    override fun getData(): Map<String, Any> {
        val properties = System.getProperties()
        val result = mutableMapOf<String, Any>()

        for (name in properties.stringPropertyNames()) {
            result[name] = properties.getProperty(name)
        }

        return if (namespace == null) {
            result
        } else {
            mapOf(namespace to result)
        }
    }
}

infix fun Configuration.overrides(other: Configuration): Configuration {
    return MapConfiguration((this.getData() overrides other.getData()) as Map<String, Any>)
}

private infix fun Map<*, *>.overrides(other: Map<*, *>): Map<*, *> {
    return mergeMaps(other, this)
}

private fun mergeMaps(
    first: Map<*, *>,
    second: Map<*, *>
): Map<*, *> {
    val result = first.toMutableMap()
    for ((k,v) in second) {
        when (v) {
            is Map<*, *> -> {
                if (first[k] is Map<*, *>) {
                    result[k] = mergeMaps(first[k] as Map<*, *>, v)
                } else {
                    result[k] = v
                }
            }
            else -> {
                result[k] = v
            }
        }
    }

    return result
}