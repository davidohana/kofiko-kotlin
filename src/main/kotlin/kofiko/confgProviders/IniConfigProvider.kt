@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.lang.reflect.Type
import java.util.*


class IniSource {
    lateinit var iniContent: String

    fun content(content: String): IniSource {
        this.iniContent = content
        return this
    }

    fun file(file: File, mustExist: Boolean = false): IniSource {
        if (!mustExist && !file.exists())
            this.iniContent = ""
        this.iniContent = file.readText()
        return this
    }

    fun filename(name: String, mustExist: Boolean = false): IniSource {
        return file(File(name), mustExist)
    }

    fun checkContentExist() {
        if (!this::iniContent.isInitialized)
            throw IllegalStateException("Content was not set. Probably forgot to call one of the content input functions.")
    }
}

class IniConfigProvider(source: IniSource) : KofikoConfigProvider {
    init {
        source.checkContentExist()
    }

    val sectionNameToProps = parseINI(source.iniContent)

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionProperties = sectionNameToProps[section] ?: return null
        return sectionProperties[option]
    }

    companion object {
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
    }
}

@Suppress("unused")
class IniFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "ini")
            return IniConfigProvider(IniSource().file(file))
        return null
    }
}


fun KofikoSettings.add(
    iniSource: IniSource
): KofikoSettings {
    this.configProviders.add(IniConfigProvider(iniSource))
    return this
}
