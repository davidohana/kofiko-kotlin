package kofiko

open class DefaultNameLookupProvider : NameLookupProvider {
    var allowUpper = true
    var allowLower = true
    var allowOriginal = true
    var allowSnakeUpper = true
    var allowSnakeLower = true
    var allowKebabLower = true
    var allowKebabUpper = true
    var allowUpperFirstLetter = true

    var sectionLookupDeleteTokens = mutableListOf("Config", "Settings", "Cfg", "Section")

    fun getCaseLookups(term: String): List<String> {
        val lookups = mutableSetOf<String>()
        if (allowOriginal)
            lookups.add(term)
        if (allowUpper)
            lookups.add(term.toUpperCase())
        if (allowLower)
            lookups.add(term.toLowerCase())
        if (allowUpperFirstLetter)
            lookups.add(term.first().toUpperCase() + term.substring(1))

        if (allowSnakeUpper || allowSnakeLower) {
            val snakeOriginal = separateCamelCase(term, "_")
            if (allowSnakeLower)
                lookups.add(snakeOriginal.toUpperCase())
            if (allowSnakeUpper)
                lookups.add(snakeOriginal.toLowerCase())
        }

        if (allowKebabUpper || allowKebabLower) {
            val kebabOriginal = separateCamelCase(term, "-")
            if (allowKebabLower)
                lookups.add(kebabOriginal.toUpperCase())
            if (allowKebabUpper)
                lookups.add(kebabOriginal.toLowerCase())
        }
        return lookups.toList()
    }

    override fun getSectionLookups(sectionName: String): List<String> {
        val lookups = mutableSetOf<String>()
        lookups.addAll(getCaseLookups(sectionName))

        var sectionWithoutTokens = sectionName
        for (token in sectionLookupDeleteTokens)
            sectionWithoutTokens = sectionWithoutTokens.replace(token, "")

        // if we get empty section name after all deletions, revert to original name
        if (sectionWithoutTokens.isEmpty())
            sectionWithoutTokens = sectionName

        if (sectionWithoutTokens != sectionName)
            lookups.addAll(getCaseLookups(sectionWithoutTokens))

        return lookups.toList()
    }

    override fun getOptionLookups(optionName: String): List<String> {
        return getCaseLookups(optionName)
    }
}