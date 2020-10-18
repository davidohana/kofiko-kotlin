package kofiko

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal fun getInheritedDeclaredFields(c: Class<*>): List<Field> {
    val fields = mutableListOf<Field>()
    var clazz: Class<*>? = c
    while (clazz != null) {
        fields.addAll(clazz.declaredFields)
        clazz = clazz.superclass
    }
    return fields
}

internal fun <T> getOverridableFields(c: Class<T>, includeReadOnly: Boolean): List<Field> {
    val allFields = getInheritedDeclaredFields(c)

    val fields = allFields
        .filter { Modifier.isPublic(it.modifiers) }
        .filter { includeReadOnly || !Modifier.isFinal(it.modifiers) }
        .filter { it.name != "Companion" }
        .toMutableList()

    val backingFields = allFields
        .filter { Modifier.isPrivate(it.modifiers) }
        .filter { hasPublicGetSet(it, needGet = true, needSet = !includeReadOnly) }

    fields.addAll(backingFields)
    return fields.toList()
}

internal fun separateCamelCase(term: String, separator: String = "_"): String {
    val regex = "([a-z])([A-Z]+)"
    var str = term

    val replacement = "$1$separator$2"
    str = str.replace(regex.toRegex(), replacement)
    return str
}

internal fun hasPublicGetSet(f: Field, needGet: Boolean, needSet: Boolean): Boolean {
    val methodFieldName = f.name.first().toUpperCase() + f.name.substring(1)
    val methods = f.declaringClass.methods

    @Suppress("UNUSED_VARIABLE")
    if (needGet) {
        val getMethod = methods
            .firstOrNull { it.name == "get$methodFieldName" && it.parameterCount == 0 } ?: return false
    }
    if (needSet) {
        val setMethod = methods
            .firstOrNull { it.name == "set$methodFieldName" && it.parameterCount == 1 } ?: return false
        // check that this method is actually related to the field
        if (setMethod.parameterTypes.first() != f.type)
            return false
    }

    if (!f.trySetAccessible())
        return false

    return true
}


internal fun getSectionName(obj: Any): String {
    val clazz = obj::class.java
    val annotation = clazz.getAnnotation(ConfigName::class.java)
    if (annotation != null && annotation.name.isNotBlank())
        return annotation.name

    val prefix = clazz.packageName + "."
    val name = clazz.canonicalName ?: clazz.simpleName
    return name.removePrefix(prefix)
}

internal fun getOptionName(field: Field): String {
    val annotation = field.getAnnotation(ConfigName::class.java)
    if (annotation != null && annotation.name.isNotBlank())
        return annotation.name
    return field.name
}

internal fun isSecretOption(field: Field): Boolean {
    return field.isAnnotationPresent(Secret::class.java)
}

internal fun parseText(parsers: List<TextParser>, textValue: String, targetType: Type): Any {
    for (parser in parsers) {
        val parseResult = parser.parse(textValue, targetType)
        if (parseResult != null) {
            //println("$textValue parsed to $targetType by $parser")
            return parseResult
        }
    }

    throw NotImplementedError(
        "conversion of '$textValue' to $targetType is not supported by any of the registered parsers"
    )
}

internal fun isGenericContainer(type: Type, expectedRawType: Class<*>): Boolean {
    if (type !is ParameterizedType)
        return false

    val rawType = type.rawType as Class<*>

    return rawType.isAssignableFrom(expectedRawType)
}

internal fun convertStringToList(textValue: String, targetType: Type, settings: KofikoSettings): List<Any> {
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
