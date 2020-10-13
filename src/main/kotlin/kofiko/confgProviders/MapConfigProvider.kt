@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

open class MapConfigProvider(
    val content: Map<String, String>,
    val keyPrefix: String = "",
    val sectionToOptionSeparator: String = "_",
) : KofikoConfigProvider {

    fun getKey(section: String, option: String): String {
        val tokens = mutableListOf<String>()
        if (keyPrefix != "")
            tokens.add(keyPrefix)
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
        return content[key]
    }

}
