package hydro.engine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class PropertiesConfigurationSource: ConfigData {

    private val loaded: Map<String, Any>

    constructor(input: InputStream) {
        loaded = try {
            JavaPropsMapper().readValue(input, Properties::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            Properties()
        } as Map<String, Any>
    }

    constructor(file: File) : this(FileInputStream(file))


    override val data: Map<String, Any>
        get() {
            return MapConfigurationSource(loaded)
        }

}

class YAMLConfigurationSource: ConfigData {

    private val loaded: Map<String, Any>

    constructor(input: InputStream) {
        loaded = try {
            ObjectMapper(YAMLFactory()).registerModule(KotlinModule()).readValue(input, object: TypeReference<HashMap<String, Any>>(){})
        } catch (e: IOException) {
            e.printStackTrace()
            emptyMap()
        }
    }

    constructor(file: File) : this(FileInputStream(file))


    override val data: Map<String, Any>
        get() {
            return MapConfigurationSource(loaded)
        }

}

class MapConfigurationSource(
    private val input: Map<String, Any>
): ConfigData() {

    override val data: Map<String, Any>
        get() = input
}

infix fun Map<*, *>.overrides(other: Map<*, *>): Map<*, *> {
    return mergeMaps(other, this)
}

infix fun ConfigData.overrides(other: ConfigData): ConfigData {
    return MapConfigurationSource(
            (this.data overrides other.data) as Map<String, Any>
        )
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


abstract class ConfigData: Map<String, Any> {

    abstract val data: Map<String, Any>

    override val entries: Set<Map.Entry<String, Any>>
        get() = data.entries
    override val keys: Set<String>
        get() = data.keys
    override val size: Int
        get() = data.size
    override val values: Collection<Any>
        get() = data.values

    override fun containsKey(key: String): Boolean = data.containsKey(key)

    override fun containsValue(value: Any): Boolean = data.containsValue(value)

    override fun isEmpty(): Boolean = data.isEmpty()

    fun getNested(key: String): Any? {
        val parts = key.split(".")
        var d = data
        var res: Any? = null

        for (part in parts) {
            if (part in d) {
                res = d[part]

                if (d[part] is Map<*, *>) {
                    d = d[part] as Map<String, Any>
                }
            } else {
                break
            }
        }

        return res
    }

    override fun get(key: String): Any? {
        return data[key]
    }

    override fun toString(): String {
        return data.toString()
    }
}