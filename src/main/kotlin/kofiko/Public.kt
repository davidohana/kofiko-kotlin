package kofiko

import java.lang.reflect.Type


fun interface KofikoConfigProvider {
    fun read(section: String, option: String, type: Type, typeConverter: TextToTypeConverter): Any?
}

fun interface TextToTypeConverter {
    fun convert(textValue: String, targetType: Type): Any
}

fun interface OverrideNotifier {
    fun accept(override: FieldOverride)
}

interface ProfileSupport {
    fun setProfile(profileName: String)
}

class KofikoSettings {
    var caseMappingAllowUpper = true
    var caseMappingAllowLower = true
    var caseMappingAllowOriginal = true
    var caseMappingAllowSnakeUpper = true
    var caseMappingAllowSnakeLower = true
    var caseMappingAllowUpperFirstLetter = true
    var sectionLookupDeleteTokens = listOf("Config", "Settings", "Cfg")
    var configProviders = mutableListOf<KofikoConfigProvider>()
    var listSeparator = ","
    var keyToValSeparator = ":"
    var appendToDicts = true
    var appendToLists = false
    var clearContainerPrefix = "^C|"
    var appendContainerPrefix = "^A|"
    var onOverride: OverrideNotifier = OverrideNotifier { }
}

data class FieldOverride(
    val sectionName: String, val optionName: String,
    val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${optionName} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
