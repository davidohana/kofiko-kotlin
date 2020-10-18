package kofiko

import com.fasterxml.jackson.databind.ObjectMapper
import kofiko.parsers.*

/**
 * Customization options for the configuration manager.
 */
class KofikoSettings() {

    /**
     * Creates a new instance.
     *
     * @param providers List of configuration sources.
     * Typically, at least one configuration provider shall be specified.
     */
    constructor(vararg providers: KofikoConfigProvider) : this() {
        configProviders.addAll(providers)
    }

    /**
     * Provides alternatives for search of config section names and config options names in config sources.
     */
    var nameLookup = DefaultNameLookupProvider()

    /**
     * List of configuration source providers. Those will be evaluated in list order until first match is found.
     */
    var configProviders = mutableListOf<KofikoConfigProvider>()

    /**
     * Defines the separator string between items when parsing [List], [Map], [Set] data types.
     * Default is ",".
     *
     * For example: `red,green,orange`.
     */
    var listSeparator = ","

    /**
     * Defines the separator string between key and value when parsing [Map] data type.
     * Default is ":".
     *
     * For example: `logs:100,alerts:200`.
     */
    var keyToValSeparator = ":"

    /**
     * For [List] type, whether to append to default list value or replace entire list.
     * Default is false (replace).
     */
    var appendToMaps = true

    /**
     * For [Set] type, whether to append to default set value or replace entire set.
     * Default is false (replace).
     */
    var appendToSets = false

    /**
     * For [Map] type, whether to append/overwrite default map keys value or replace entire map.
     * Default is true (append/overwrite).
     */
    var appendToLists = false

    /**
     * Regardless of the `appendTo*` setting, when override string is prefixed by this string,
     * Kofiko will clear the container before adding new elements.
     *
     * Default prefix is `^C|`. For example `"^C|item1,item2"`.
     */
    var clearContainerPrefix = "^C|"

    /**
     * Regardless of the `appendTo*` setting, when override string is prefixed by this string,
     * Kofiko will append/overwrite elements in the container.
     *
     * Default prefix is `^A|`. For example `"^A|key1:val1,key2:val2"`.
     */
    var appendContainerPrefix = "^A|"

    /**
     * An action to perform when a field value is changed from default value.
     *
     * Default action does nothing. It is possible to set to [PrintOverrideNotifier] or [LogOverrideNotifier]
     * in order to print message to stdout/log respectively.
     */
    var onOverride: OverrideNotifier = OverrideNotifier { }

    /**
     * When parsing `Boolean` type, defines the strings that will be parsed to true. Case insensitive.
     */
    var booleanTrueStates = mutableSetOf("true", "1", "on", "yes", "t", "y")

    /**
     * When parsing `Boolean` type, defines the strings that will be parsed to false. Case insensitive.
     */
    var booleanFalseStates = mutableSetOf("false", "0", "off", "no", "f", "n")

    /**
     * List of test parsers. Those will be invoked in the list order when trying to parse a string into a typed field
     * until the first returns a non-null value.
     */
    var textParsers = listOf(
        BooleanParser(this),
        LogLevelParser(),
        ConciseListParser(this),
        ConciseMapParser(this),
        ConciseSetParser(this),
        JsonParser(this),
    )

    /**
     * When set to true, allow overriding of final non-static Kotlin Properties (val), Kotlin properties without a public setter
     * and final JVM fields.
     * **Important note**: This settings still does not allow overriding val properties in Kotlin `object`
     * and static final java fields due to JVM limitation.
     *
     * When set to false, configuration will be applicable only to non-final Kotlin Properties (var) with public
     * getter and setter and to public non-final JVM fields.
     *
     * Configuration is never applied to Kotlin properties where both getter and setter are private/protected,
     * and to private/protected JVM fields.
     */
    var configureReadonlyProperties = true

    /**
     * The [ObjectMapper] that will be used by [JsonParser] when trying to parse strings to typed fields.
     */
    var objectMapper = ObjectMapper()

    /**
     * Adds a configuration provider.
     */
    fun add(provider: KofikoConfigProvider) = apply { configProviders.add(provider) }
}


