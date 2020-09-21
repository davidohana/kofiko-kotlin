package kofiko

import com.fasterxml.jackson.databind.ObjectMapper
import kofiko.parsers.*
import java.lang.reflect.Field
import java.lang.reflect.Type


fun interface KofikoConfigProvider {
    fun read(section: String, option: String, type: Type): Any?
}

interface TextParser {
    fun parse(textValue: String, targetType: Type): Any?
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
annotation class ConfigOption(val name: String = "", val secret: Boolean = false)

class CaseMappingSettings {
    var allowUpper = true
    var allowLower = true
    var allowOriginal = true
    var allowSnakeUpper = true
    var allowSnakeLower = true
    var allowKebabLower = true
    var allowKebabUpper = true
    var allowUpperFirstLetter = true
}

class KofikoSettings() {
    constructor(vararg providers: KofikoConfigProvider) : this() {
        configProviders.addAll(providers)
    }

    var caseMapping = CaseMappingSettings()
    var sectionLookupDeleteTokens = mutableListOf("Config", "Settings", "Cfg")
    var configProviders = mutableListOf<KofikoConfigProvider>()
    var listSeparator = ","
    var keyToValSeparator = ":"
    var appendToDicts = true
    var appendToLists = false
    var appendToSets = false
    var clearContainerPrefix = "^C|"
    var appendContainerPrefix = "^A|"
    var onOverride: OverrideNotifier = OverrideNotifier { }
    var booleanTrueStates = mutableSetOf("true", "1", "on", "yes", "t", "y")
    var booleanFalseStates = mutableSetOf("false", "0", "off", "no", "f", "n")
    var objectMapper = ObjectMapper()
    var textParsers = listOf(
        BooleanParser(this),
        JsonParser(this),
        ConciseListParser(this),
        ConciseMapParser(this),
        ConciseSetParser(this),
    )
}

data class FieldOverride(
    val field: Field, val sectionName: String, val optionName: String,
    val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${optionName} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
