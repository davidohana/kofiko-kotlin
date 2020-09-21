package kofiko

import java.io.File
import java.lang.reflect.Type
import java.util.*


class ConfigProviderIni(iniFile: File) : KofikoConfigProvider {
    constructor(iniFileName: String) : this(File(iniFileName))

    val sectionNameToProps = parseINI(iniFile)

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
        private fun parseINI(file: File): Map<String, Properties> {
            if (!file.exists())
                return emptyMap()

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
            }.load(file.reader())
            return result
        }
    }
}