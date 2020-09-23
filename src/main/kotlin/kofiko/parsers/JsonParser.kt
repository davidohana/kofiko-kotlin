package kofiko.parsers

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.type.TypeFactory
import kofiko.*
import java.lang.reflect.Type

@Suppress("LiftReturnOrAssignment")
class JsonParser(val settings: KofikoSettings) : TextParser {
    fun tryParse(text: String, reader: ObjectReader): Any? {
        try {
            return reader.readValue(text)
        } catch (x: Throwable) {
            return null
        }
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        val reader: ObjectReader
        try {
            val javaType = TypeFactory.defaultInstance().constructType(targetType)
            reader = settings.objectMapper.readerFor(javaType)!!
        } catch (x: Throwable) {
            return null
        }
        val quotedText = "\"" + textValue.replace("\"", "\\\"") + "\""
        val parsed = tryParse(quotedText, reader)
        if (parsed != null)
            return parsed
        return tryParse(textValue, reader)
    }
}

