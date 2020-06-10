package hydro.converters

internal class IntToStringConverter: TypeConverter<Int, String> {
    override fun convert(input: Int): String = input.toString()
}

internal class IntToFloatConverter: TypeConverter<Int, Float> {
    override fun convert(input: Int): Float = input.toFloat()
}

internal class IntToDoubleConverter: TypeConverter<Int, Double> {
    override fun convert(input: Int): Double = input.toDouble()
}

internal class IntToByteConverter: TypeConverter<Int, Byte> {
    override fun convert(input: Int): Byte = input.toByte()
}

internal class IntToShortConverter: TypeConverter<Int, Short> {
    override fun convert(input: Int): Short = input.toShort()
}



internal class StringToIntConverter: TypeConverter<String, Int> {
    override fun convert(input: String): Int = input.toInt()
}

internal class StringToFloatConverter: TypeConverter<String, Float> {
    override fun convert(input: String): Float = input.toFloat()
}

internal class StringToDoubleConverter: TypeConverter<String, Double> {
    override fun convert(input: String): Double = input.toDouble()
}

internal class StringToShortConverter: TypeConverter<String, Short> {
    override fun convert(input: String): Short = input.toShort()
}

internal class StringToByteConverter: TypeConverter<String, Byte> {
    override fun convert(input: String): Byte = input.toByte()
}

internal class StringToBooleanConverter: TypeConverter<String, Boolean> {
    override fun convert(input: String): Boolean = input.toBoolean()
}



internal class FloatToIntConverter: TypeConverter<Float, Int> {
    override fun convert(input: Float): Int = input.toInt()
}

internal class FloatToDoubleConverter: TypeConverter<Float, Double> {
    override fun convert(input: Float): Double = input.toDouble()
}



internal class DoubleToIntConverter: TypeConverter<Double, Int> {
    override fun convert(input: Double): Int = input.toInt()
}

internal class DoubleToFloatConverter: TypeConverter<Double, Float> {
    override fun convert(input: Double): Float = input.toFloat()
}



internal class ByteToIntConverter: TypeConverter<Byte, Int> {
    override fun convert(input: Byte): Int = input.toInt()
}