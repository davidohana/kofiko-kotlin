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

    var sectionLookupDeleteTerms = mutableListOf("Config", "Settings", "Cfg", "Section")

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

        var sectionWithoutTerms = sectionName
        for (term in sectionLookupDeleteTerms)
            sectionWithoutTerms = sectionWithoutTerms.replace(term, "")

        // if we get empty section name after all deletions, revert to original name
        if (sectionWithoutTerms.isEmpty())
            sectionWithoutTerms = sectionName

        if (sectionWithoutTerms != sectionName)
            lookups.addAll(getCaseLookups(sectionWithoutTerms))

        return lookups.toList()
    }

    override fun getOptionLookups(optionName: String): List<String> {
        return getCaseLookups(optionName)
    }
}