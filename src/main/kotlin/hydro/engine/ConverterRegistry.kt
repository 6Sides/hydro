package hydro.engine

import hydro.converters.*
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf


class ConverterRegistry {

    private val converters = mutableMapOf<Pair<KType, KType>, TypeConverter<*,*>>()

    init {
        registerConverters(
            IntToByteConverter(),
            IntToDoubleConverter(),
            IntToFloatConverter(),
            IntToShortConverter(),
            IntToStringConverter(),

            StringToBooleanConverter(),
            StringToByteConverter(),
            StringToDoubleConverter(),
            StringToFloatConverter(),
            StringToIntConverter(),
            StringToShortConverter(),

            DoubleToIntConverter(),
            DoubleToFloatConverter(),

            FloatToIntConverter(),
            FloatToDoubleConverter(),

            ByteToIntConverter()
        )
    }

    fun registerConverter(converter: TypeConverter<out Any, out Any>) {
        converters[getConverterTypes(converter)] = converter
    }

    fun registerConverters(vararg converters: TypeConverter<out Any, out Any>) {
        converters.forEach {
            registerConverter(it)
        }
    }

    fun getConverter(input: KType, output: KType): TypeConverter<in Any, *> {
        // If output is a supertype of the input, no conversion needs to take place
        if (input::class.isSubclassOf(output::class)) {
            return object : TypeConverter<Any, Any> {
                override fun convert(input: Any): Any {
                    return input
                }
            }
        }

        return converters[Pair(input, output)] as? TypeConverter<in Any, *> ?: error("No converter registered for that pair ($input, $output)")
    }

    private fun getConverterTypes(converter: TypeConverter<*, *>): Pair<KType, KType> {
        with (converter::class.supertypes[0].arguments) {
            return Pair(
                this[0].type!!,
                this[1].type!!
            )
        }
    }

}