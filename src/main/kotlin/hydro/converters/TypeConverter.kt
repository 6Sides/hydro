package hydro.converters

interface TypeConverter<T, R> {

    fun convert(input: T): R

}