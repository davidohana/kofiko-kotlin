package kofiko

import com.fasterxml.jackson.databind.ObjectMapper
import kofiko.parsers.*

class KofikoSettings() {
    constructor(vararg providers: KofikoConfigProvider) : this() {
        configProviders.addAll(providers)
    }

    var nameLookup = DefaultNameLookupProvider()
    var configProviders = mutableListOf<KofikoConfigProvider>()
    var listSeparator = ","
    var keyToValSeparator = ":"
    var appendToMaps = true
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
        LogLevelParser(),
        ConciseListParser(this),
        ConciseMapParser(this),
        ConciseSetParser(this),
        JsonParser(this),
    )

    fun add(provider: KofikoConfigProvider) = apply { configProviders.add(provider) }
}


