package kofiko.parsers

import kofiko.KofikoSettings
import kofiko.TextParser
import kofiko.convertStringToList
import kofiko.isGenericContainer
import java.lang.reflect.Type

class ConciseSetParser(val settings: KofikoSettings) : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (isGenericContainer(targetType, Set::class.java)) {
            try {
                val list = convertStringToList(textValue, targetType, settings)
                return list.toSet()
            } catch (ex: Throwable) {
                return null
            }
        }

        return null
    }
}