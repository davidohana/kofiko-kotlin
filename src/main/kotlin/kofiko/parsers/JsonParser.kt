package kofiko.parsers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.type.TypeFactory
import kofiko.TextParser
import java.lang.reflect.Type

@Suppress("LiftReturnOrAssignment")
class JsonParser : TextParser {
    val objectMapper: ObjectMapper = ObjectMapper()

    fun tryParse(textValue: String, reader: ObjectReader): Any? {
        try {
            return reader.readValue(textValue)
        } catch (x: Throwable) {
            return null
        }
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        val reader: ObjectReader
        try {
            val javaType = TypeFactory.defaultInstance().constructType(targetType)
            reader = objectMapper.readerFor(javaType)!!
        } catch (x: Throwable) {
            return null
        }
        val parsed = tryParse("\"" + textValue + "\"", reader)
        if (parsed != null)
            return parsed
        return tryParse(textValue, reader)
    }
}