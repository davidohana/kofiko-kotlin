package kofiko

import java.lang.reflect.Field
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

@Target(AnnotationTarget.CLASS)
annotation class ConfigSection(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class Secret

class CaseMappingSettings {
    var allowUpper = true
    var allowLower = true
    var allowOriginal = true
    var allowSnakeUpper = true
    var allowSnakeLower = true
    var allowUpperFirstLetter = true
}

class KofikoSettings {
    var caseMapping = CaseMappingSettings()
    var sectionLookupDeleteTokens = mutableListOf("Config", "Settings", "Cfg")
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
    val sectionName: String, val field: Field, val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${field.name} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
