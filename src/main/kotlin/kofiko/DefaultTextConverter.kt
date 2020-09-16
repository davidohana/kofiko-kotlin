package kofiko

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class DefaultTextConverter(val settings: KofikoSettings) : TextToTypeConverter {

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
                    val typedKey = convert(it.first, mapKeyType)
                    val typedVal = convert(it.second, mapValueType)
                    Pair(typedKey, typedVal)
                }
                .toMap()
            resultMap.putAll(parsedMap)
        }

        return resultMap
    }

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
        val parsedList = textElements.map { convert(it, listValueType) }

        resultList.addAll(parsedList)
        return resultList
    }

    val booleanTrueStates = setOf("true", "1", "on", "yes", "t", "y")
    val booleanFalseStates = setOf("false", "0", "off", "no", "f", "n")

    fun parseBooleanExtended(text: String): Boolean {
        val lower = text.toLowerCase()
        if (lower in booleanTrueStates)
            return true
        if (lower in booleanFalseStates)
            return false
        throw IllegalArgumentException("Not a boolean: $text")
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(textValue: String, targetType: Type): Any {

        if (String::class.java == targetType)
            return textValue
        if (Boolean::class.java == targetType || targetType.typeName == java.lang.Boolean::class.java.name)
            return parseBooleanExtended(textValue)
        if (Byte::class.java == targetType || targetType.typeName == java.lang.Byte::class.java.name)
            return java.lang.Byte.parseByte(textValue)
        if (Short::class.java == targetType || targetType.typeName == java.lang.Short::class.java.name)
            return java.lang.Short.parseShort(textValue)
        if (Int::class.java == targetType || targetType.typeName == java.lang.Integer::class.java.name)
            return Integer.parseInt(textValue)
        if (Long::class.java == targetType || targetType.typeName == java.lang.Long::class.java.name)
            return java.lang.Long.parseLong(textValue)
        if (Float::class.java == targetType || targetType.typeName == java.lang.Float::class.java.name)
            return java.lang.Float.parseFloat(textValue)
        if (Double::class.java == targetType || targetType.typeName == java.lang.Double::class.java.name)
            return java.lang.Double.parseDouble(textValue)

        var targetClass: Class<*>? = null
        if (targetType is Class<*>)
            targetClass = targetType

        if (targetClass != null) {
            if (targetClass.isEnum) {
                val method = targetClass.getMethod("valueOf", String::class.java)
                val enumVal = method.invoke(null, textValue)
                return enumVal
            }

            if (targetClass.isAssignableFrom(List::class.java)) {
                return textValue.split(settings.listSeparator)
            }
        }

        var parametrizedType: ParameterizedType? = null
        var rawType: Class<*>? = null
        if (targetType is ParameterizedType) {
            parametrizedType = targetType
            rawType = parametrizedType.rawType as Class<*>
        }

        if (parametrizedType != null && rawType != null) {
            if (rawType.isAssignableFrom(Class::class.java)) {
                return Class.forName(textValue)
            }

            if (rawType.isAssignableFrom(List::class.java)) {
                return convertStringToList(textValue, targetType)
            }

            if (rawType.isAssignableFrom(Map::class.java)) {
                return convertStringToMap(textValue, targetType)
            }
        }


        throw NotImplementedError("conversion of '$textValue' to $targetType is not supported yet")
    }
}