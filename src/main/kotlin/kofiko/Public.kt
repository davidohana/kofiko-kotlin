package kofiko

import java.lang.reflect.Type


interface KofikoConfigProvider {
    fun read(section: String, option: String, type: Type, typeConverter: TextToTypeConverter): Any?
}

interface TextToTypeConverter {
    fun convert(textValue: String, targetType: Type): Any
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
    var onOverride: (String, String, Any, Any, String) -> Unit = { _, _, _, _, _ -> Unit }
}

data class FieldOverride(val fieldName: String, val oldValue: Any, val newValue: Any, val byProvider: String)
