package kofiko

/**
 * Build-in name lookup provider, that provides alternatives to name search by different casing styles and also
 * supports omitting specific terms from section names.
 */
open class DefaultNameLookupProvider : NameLookupProvider {
    /**
     * UPPERCASE (e.g hostName --> HOSTNAME)
     */
    var allowUpper = true

    /**
     * lowercase (e.g hostName --> hostname)
     */
    var allowLower = true

    /**
     * Original case (e.g hostName --> hostName)
     */
    var allowOriginal = true

    /**
     * SNAKE_UPPER case (e.g hostName --> HOST_NAME)
     */
    var allowSnakeUpper = true

    /**
     * snake_lower case (e.g hostName --> host_name)
     */
    var allowSnakeLower = true

    /**
     * KEBAB-UPPEr case (e.g hostName --> HOST-NAME)
     */
    var allowKebabUpper = true

    /**
     * kebab-lower case (e.g hostName --> host-name)
     */
    var allowKebabLower = true

    /**
     * Upperfirstletter case (e.g hostName --> Hostname)
     */
    var allowUpperFirstLetter = true

    /**
     * If any of the specified strings is contained in section name, an alternative without this
     * term will ge provided as well.
     */
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