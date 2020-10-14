@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

open class MapConfigProvider(
    var content: Map<String, String>,
    var keyPrefix: String = "",
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

fun KofikoSettings.addMap(content: Map<String, String>, init: MapConfigProvider.() -> Unit = {}) = this.apply {
    val provider = MapConfigProvider(content)
    provider.init()
    this.configProviders.add(provider)
}

