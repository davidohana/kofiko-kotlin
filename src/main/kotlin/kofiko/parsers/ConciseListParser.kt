package kofiko.parsers

import kofiko.KofikoSettings
import kofiko.TextParser
import kofiko.isGenericContainer
import kofiko.parseText
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ConciseListParser(val settings: KofikoSettings) : TextParser {

    fun convertStringToList(textValue: String, targetType: Type): Any {
        @Suppress("NAME_SHADOWING")
        var textValue = textValue

        val resultList = mutableListOf<Any>()
        if (textValue.startsWith(settings.clearContainerPrefix)) {
            textValue = textValue.substring(settings.clearContainerPrefix.length)
            resultList.add(settings.clearContainerPrefix)
        } else if (textValue.startsWith(settings.appendContainerPrefix)) {
            textValue = textValue.substring(settings.appendContainerPrefix.length)
            resultList.add(settings.appendContainerPrefix)
        }

        val textElements = textValue.split(settings.listSeparator)
        val parameterizedType = targetType as ParameterizedType
        val listValueType = parameterizedType.actualTypeArguments.first()
        val parsedList = textElements.map {
            parseText(settings.textParsers, it, listValueType)
        }

        resultList.addAll(parsedList)
        return resultList
    }

    override fun parse(textValue: String, targetType: Type): Any? {
        if (!isGenericContainer(targetType, List::class.java))
            return null

        return try {
            convertStringToList(textValue, targetType)
        } catch (ex: Exception) {
            null
        }
    }
}