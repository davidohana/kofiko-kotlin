package kofiko.parsers

import kofiko.KofikoSettings
import kofiko.TextParser
import kofiko.isGenericContainer
import kofiko.parseText
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ConciseMapParser(val settings: KofikoSettings) : TextParser {

    fun convertStringToMap(textValue: String, targetType: Type): Any {
        @Suppress("NAME_SHADOWING")
        var textValue = textValue

        val resultMap = mutableMapOf<Any, Any>()
        if (textValue.startsWith(settings.clearContainerPrefix)) {
            textValue = textValue.substring(settings.clearContainerPrefix.length)
            resultMap[settings.clearContainerPrefix] = true
        } else if (textValue.startsWith(settings.appendContainerPrefix)) {
            textValue = textValue.substring(settings.appendContainerPrefix.length)
            resultMap[settings.appendContainerPrefix] = true
        }

        if (textValue.isNotEmpty()) {
            val parameterizedType = targetType as ParameterizedType
            val mapKeyType = (parameterizedType.actualTypeArguments[0] as Class<*>)
            val mapValueType = (parameterizedType.actualTypeArguments[1] as Class<*>)

            val parsedMap = textValue
                .split(settings.listSeparator)
                .map { it.split(settings.keyToValSeparator, limit = 2) }
                .map {
                    check(it.size == 2) { "Illegal map format: '$textValue'" }
                    it
                }
                .map { Pair(it[0], it[1]) }
                .map {
                    val typedKey = parseText(settings.textParsers, it.first, mapKeyType)
                    val typedVal = parseText(settings.textParsers, it.second, mapValueType)
                    Pair(typedKey, typedVal)
                }
                .toMap()
            resultMap.putAll(parsedMap)
        }

        return resultMap
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        if (!isGenericContainer(targetType, Map::class.java))
            return null

        try {
            return convertStringToMap(textValue, targetType)
        } catch (ex: Throwable) {
            return null
        }
    }
}