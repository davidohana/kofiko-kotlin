package kofiko

import com.fasterxml.jackson.databind.ObjectMapper
import kofiko.parsers.*

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

    var nameLookupProvider = DefaultNameLookupProvider()
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
        LogLevelParser(),
        ConciseListParser(this),
        ConciseMapParser(this),
        ConciseSetParser(this),
        JsonParser(this),
    )
}

