package kofiko.parsers

import kofiko.KofikoSettings
import kofiko.TextParser
import kofiko.convertStringToList
import kofiko.isGenericContainer
import java.lang.reflect.Type

class ConciseListParser(val settings: KofikoSettings) : TextParser {

    override fun parse(textValue: String, targetType: Type): Any? {
        if (isGenericContainer(targetType, List::class.java)) {
            try {
                return convertStringToList(textValue, targetType, settings)
            } catch (ex: Throwable) {
                return null
            }
        }

        return null
    }
}