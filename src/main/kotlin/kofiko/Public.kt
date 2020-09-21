package kofiko

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

interface NameLookupProvider {
    fun getSectionLookups(sectionName: String): List<String>
    fun getOptionLookups(optionName: String): List<String>
}


@Target(AnnotationTarget.CLASS)
annotation class ConfigSection(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class ConfigOption(val name: String = "", val secret: Boolean = false)


data class FieldOverride(
    val field: Field, val sectionName: String, val optionName: String,
    val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${optionName} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
