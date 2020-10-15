package kofiko

import java.lang.reflect.Field
import java.lang.reflect.Type


/***
 * Represents a configuration provider.
 */
fun interface KofikoConfigProvider {
    /**
     * Retrieves a value from the configuration by mapping a section name + option name combination to a value.
     * For providers that are type-aware, the returned value can be typed (according to [type]).
     * Otherwise it can be [String], and the configuration manager will attempt to convert parse it to the expected
     * field value using once of the parsers in [KofikoSettings.textParsers].
     *
     * The result can be null if no value found.
     *
     * @param section The section name, which is usually derived from class name of the configuration object.
     * @param option The option name, which is usually derived from field name of the configuration object.
     *
     * @return value read from configuration source (typed or string), or null if no such value found.
     */
    fun read(section: String, option: String, type: Type): Any?
}

/**
 * Represents a factory that is able to create [KofikoConfigProvider] from filename if it supports that file type.
 *
 * All implementations of this interface are located and instantiated using [java.util.ServiceLoader].
 */
fun interface FileProviderFactory {

    /**
     * Creates a config provider that supports the specified type of filename (typically according to file extension)
     * Should return null if file type is not supported by this provider.
     */
    fun createConfigProvider(filename: String): KofikoConfigProvider?
}

/**
 * Represents a parser that can convert text value into a typed value.
 */
fun interface TextParser {
    /**
     * Converts the specified text value into a typed object of the specified type.
     * May return null if such conversion is not supported.
     */
    fun parse(textValue: String, targetType: Type): Any?
}

/**
 * Represents a custom logic that is invoked when a field in a configuration object is changed from default value.
 */
fun interface OverrideNotifier {
    fun accept(override: FieldOverride)
}

/**
 * A configuration object that support profiles for hard-coded configuration sets.
 */
interface ProfileSupport {
    /**
     * Modifies configuration fields according to the specified profile name.
     */
    fun setProfile(profileName: String)
}

/**
 * Provides alternative strings for lookup of names of sections and options. For example, different
 * casing permutation of the original name.
 *
 * Configuration manager will try to look up for matching configuration values in all of the alternatives
 * according the the specified order (first wins).
 */
interface NameLookupProvider {
    fun getSectionLookups(sectionName: String): List<String>
    fun getOptionLookups(optionName: String): List<String>
}


/**
 * Sets a custom name for a config section (class) or config option (property)
 *
 * Default name if this field is not specified is the class/field name.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class ConfigName(val name: String)

/***
 * Flags a configuration option (property) as sensitive information. This will cause configuration manager
 * to hide old and new values of thsi field in override notifications.
 */
@Target(AnnotationTarget.FIELD)
annotation class Secret


/***
 * Information about an override (value change) of a single configuration option.
 */
data class FieldOverride(
    val field: Field, val sectionName: String, val optionName: String,
    val oldValue: Any, val newValue: Any, val byProvider: String
) {
    override fun toString(): String {
        return "${sectionName}.${optionName} was changed from <${oldValue}> to <${newValue}> by $byProvider"
    }
}
