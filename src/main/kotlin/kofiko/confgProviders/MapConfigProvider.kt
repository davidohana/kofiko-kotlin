@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

/**
 * Retrieves configuration overrides from a Java Map object.
 * By default, expected map keys should be in the format section_option=value or prefix_section_option=key
 * if [keyPrefix] is set.
 */
open class MapConfigProvider(
    /**
     * Map content
     **/
    var content: Map<String, String>,


    /**
     * Prefix to add when searching for keys in map. Default is empty.
     *
     * For example MYAPP_database_host=localhost.
     */
    var keyPrefix: String = "",

    /**
     * String that separates between section name and option name when looking up for config overrides.
     */
    var sectionToOptionSeparator: String = "_",
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

/**
 * Add a map instance variables as config provider.
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addMap(content: Map<String, String>, init: MapConfigProvider.() -> Unit = {}) = this.apply {
    val provider = MapConfigProvider(content)
    provider.init()
    this.configProviders.add(provider)
}

