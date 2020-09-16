package kofiko

import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal fun <T> getOverridableFields(c: Class<T>): List<Field> {
    val allFields = c.declaredFields

    val fields = allFields
        .filter { Modifier.isPublic(it.modifiers) }
        .filter { it.name != "Companion" }
        .toMutableList()

    val backingFields = allFields
        .filter { Modifier.isPrivate(it.modifiers) }
        .filter { hasPublicGetSet(it) }

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

internal fun getCaseLookups(term: String, settings: CaseMappingSettings): List<String> {
    val lookups = mutableSetOf<String>()
    if (settings.allowOriginal)
        lookups.add(term)
    if (settings.allowUpper)
        lookups.add(term.toUpperCase())
    if (settings.allowLower)
        lookups.add(term.toLowerCase())
    if (settings.allowUpperFirstLetter)
        lookups.add(term.first().toUpperCase() + term.substring(1))

    if (settings.allowSnakeUpper || settings.allowSnakeLower) {
        val snakeOriginal = separateCamelCase(term, "_")
        if (settings.allowSnakeLower)
            lookups.add(snakeOriginal.toUpperCase())
        if (settings.allowSnakeUpper)
            lookups.add(snakeOriginal.toLowerCase())
    }

    if (settings.allowKebabUpper || settings.allowKebabLower) {
        val kebabOriginal = separateCamelCase(term, "-")
        if (settings.allowKebabLower)
            lookups.add(kebabOriginal.toUpperCase())
        if (settings.allowKebabUpper)
            lookups.add(kebabOriginal.toLowerCase())
    }
    return lookups.toList()
}

internal fun hasPublicGetSet(f: Field): Boolean {
    val methodFieldName = f.name.first().toUpperCase() + f.name.substring(1)
    val methods = f.declaringClass.methods

    @Suppress("UNUSED_VARIABLE")
    val getMethod = methods
        .firstOrNull { it.name == "get$methodFieldName" && it.parameterCount == 0 } ?: return false
    val setMethod = methods
        .firstOrNull { it.name == "set$methodFieldName" && it.parameterCount == 1 } ?: return false
    if (setMethod.parameterTypes.first() != f.type)
        return false
    if (!f.trySetAccessible())
        return false
    return true
}


internal fun getSectionNameLookups(sectionName: String, settings: KofikoSettings): List<String> {
    val lookups = mutableSetOf<String>()
    lookups.addAll(getCaseLookups(sectionName, settings.caseMapping))

    var sectionWithoutTokens = sectionName
    for (token in settings.sectionLookupDeleteTokens)
        sectionWithoutTokens = sectionWithoutTokens.replace(token, "")

    // if we get empty section name after all deletions, revert to original name
    if (sectionWithoutTokens.isEmpty())
        sectionWithoutTokens = sectionName

    if (sectionWithoutTokens != sectionName)
        lookups.addAll(getCaseLookups(sectionWithoutTokens, settings.caseMapping))

    return lookups.toList()
}

internal fun getOptionNameLookups(optionName: String, settings: KofikoSettings): List<String> {
    return getCaseLookups(optionName, settings.caseMapping).toList()
}

internal fun getSectionName(obj: Any): String {
    val clazz = obj::class.java
    val annotationsByType = clazz.getAnnotationsByType(ConfigSection::class.java)
    if (annotationsByType.isNotEmpty())
        return annotationsByType.first().name

    val prefix = clazz.packageName + "."
    val name = clazz.canonicalName ?: clazz.simpleName
    return name.removePrefix(prefix)
}

internal fun isSecretOption(field: Field): Boolean {
    if (field.isAnnotationPresent(Secret::class.java))
        return true

    return false
}