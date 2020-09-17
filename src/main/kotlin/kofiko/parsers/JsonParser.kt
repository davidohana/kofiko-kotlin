package kofiko.parsers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import kofiko.TextParser
import java.lang.reflect.Type

class JsonParser : TextParser {
    val objectMapper: ObjectMapper = ObjectMapper()

    override fun parse(textValue: String, targetType: Type): Any? {
        @Suppress("LiftReturnOrAssignment")
        try {
            val javaType = TypeFactory.defaultInstance().constructType(targetType)
            return objectMapper.readerFor(javaType).readValue("\"" + textValue + "\"")
        } catch (x: JsonProcessingException) {
            return null
        }
    }
}