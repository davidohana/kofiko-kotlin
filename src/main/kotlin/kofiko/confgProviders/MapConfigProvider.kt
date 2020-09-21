@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

open class MapConfigProvider(
    map: Map<String, String>,
    val prefix: String = "",
    val sectionToOptionSeparator: String = "_",
    trimWhitespace: Boolean = true,
    trimQuotes: Boolean = true,
) : KofikoConfigProvider {

    private val trimmedMap: Map<String, String>

    fun stripQuotes(text: String): String {
        var quoteChar = '"'
        if (text.startsWith(quoteChar) && text.endsWith(quoteChar))
            return text.substring(1, text.length - 1)

        quoteChar = '\''
        if (text.startsWith(quoteChar) && text.endsWith(quoteChar))
            return text.substring(1, text.length - 1)

        return text
    }

    init {
        var workMap = map
        if (trimWhitespace)
            workMap = workMap
                .mapKeys { it.key.trim() }
                .mapValues { it.value.trim() }

        if (trimQuotes)
            workMap = workMap
                .mapKeys { stripQuotes(it.key) }
                .mapValues { stripQuotes(it.value) }

        trimmedMap = workMap
    }

    fun getKey(section: String, option: String): String {
        val tokens = mutableListOf<String>()
        if (prefix != "")
            tokens.add(prefix)
        tokens.add(section)
        tokens.add(option)
        return tokens.joinToString(sectionToOptionSeparator)
    }

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val key = getKey(section, option)
        return trimmedMap[key]
    }
}