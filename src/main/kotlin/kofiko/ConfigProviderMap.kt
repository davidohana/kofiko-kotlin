package kofiko

import java.lang.reflect.Type

open class ConfigProviderMap(
    val prefix: String = "",
    val sectionToOptionSeparator: String = "_",
    val map: Map<String, String>
) : KofikoConfigProvider {

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
        typeConverter: TextToTypeConverter
    ): Any? {
        val key = getKey(section, option)
        val valueStr = map[key] ?: return null
        return typeConverter.convert(valueStr, type)
    }
}