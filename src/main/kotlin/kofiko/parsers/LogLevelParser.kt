package kofiko.parsers

import kofiko.TextParser
import java.lang.reflect.Type
import java.util.logging.Level

class LogLevelParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Level::class.java == targetType)
            return Level.parse(textValue)

        return null
    }
}