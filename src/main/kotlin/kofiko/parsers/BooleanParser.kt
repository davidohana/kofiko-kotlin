package kofiko.parsers

import kofiko.KofikoSettings
import kofiko.TextParser
import java.lang.reflect.Type

class BooleanParser(val settings: KofikoSettings) : TextParser {
    fun parseBoolean(text: String): Boolean {
        val lower = text.toLowerCase()
        if (lower in settings.booleanTrueStates)
            return true
        if (lower in settings.booleanFalseStates)
            return false
        throw IllegalArgumentException("Not a boolean: $text")
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        if (Boolean::class.java == targetType || targetType.typeName == java.lang.Boolean::class.java.name)
            return parseBoolean(textValue)

        return null
    }
}