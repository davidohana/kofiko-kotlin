package kofiko

import java.lang.reflect.Type

class ConfigProviderEnv(
    val prefix: String = "",
    val sectionToOptionSeparator: String = "_",
    val env: Map<String, String> = System.getenv()!!
) : KofikoConfigProvider {

    fun getEnvKey(section: String, option: String): String {
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
        val envKey = getEnvKey(section, option)
        val valueStr = env[envKey] ?: return null
        return typeConverter.convert(valueStr, type)
    }
}