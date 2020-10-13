@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.lang.reflect.Type
import java.util.*


class IniConfigProvider(configSource: ConfigSource) : KofikoConfigProvider {

    val sectionNameToProps = parseINI(configSource.content)

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionProperties = sectionNameToProps[section] ?: return null
        return sectionProperties[option]
    }
}

// ported https://stackoverflow.com/a/41084504/978164
private fun parseINI(content: String): Map<String, Properties> {

    val result = mutableMapOf<String, Properties>()
    object : Properties() {
        private var section: Properties? = null
        override fun put(key: Any, value: Any): Any? {
            val header = (key as String + " " + value).trim { it <= ' ' }
            return if (header.startsWith("[") && header.endsWith("]")) result.put(header.substring(
                1,
                header.length - 1
            ),
                Properties().also {
                    section = it
                }) else section!!.put(key, value.toString().trim())
        }
    }.load(content.reader())
    return result
}

@Suppress("unused")
class IniFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "ini")
            return IniConfigProvider(ConfigSource(file))
        return null
    }
}
