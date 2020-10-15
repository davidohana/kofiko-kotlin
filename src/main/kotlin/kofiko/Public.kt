package kofiko

import java.lang.reflect.Field
import java.lang.reflect.Type


fun interface KofikoConfigProvider {
    fun read(section: String, option: String, type: Type): Any?
}

interface FileProviderFactory {
    fun createConfigProvider(filename: String): KofikoConfigProvider?
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


@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class ConfigName(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class Secret()


data class FieldOverride(
    val field: Field, val sectionName: String, val optionName: String,
    val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${optionName} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
