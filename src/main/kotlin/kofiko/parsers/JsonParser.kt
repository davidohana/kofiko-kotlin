package kofiko.parsers

import com.fasterxml.jackson.core.JsonProcessingException
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
        } catch (x: JsonProcessingException) {
            return null
        }
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        try {
            val javaType = TypeFactory.defaultInstance().constructType(targetType)
            val reader = objectMapper.readerFor(javaType)!!
            val parsed = tryParse("\"" + textValue + "\"", reader)
            if (parsed != null)
                return parsed
            return tryParse(textValue, reader)
        } catch (x: Exception) {
            return null
        }
    }

//    override fun parse(textValue: String, targetType: Type): Any? {
//        @Suppress("LiftReturnOrAssignment")
//        try {
//            val javaType = TypeFactory.defaultInstance().constructType(targetType)
//            val reader = objectMapper.readerFor(javaType)
//            val value = reader.readValue<Any?>("\"" + textValue + "\"")
//            return value
//        } catch (x: JsonProcessingException) {
//            return null
//        }
//    }
}