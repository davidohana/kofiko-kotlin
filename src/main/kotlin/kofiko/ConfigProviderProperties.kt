package kofiko

import java.io.File
import java.util.*

private val stripChars = charArrayOf('"', '\'')

class ConfigProviderProperties(
    properties: Properties,
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) : ConfigProviderMap(
    prefix,
    sectionToOptionSeparator,
    properties
        .mapKeys { it.key.toString().trim(*stripChars).trim() }
        .mapValues { it.value.toString().trim(*stripChars).trim() })

fun File.loadProperties(): Properties {
    val properties = Properties()
    properties.load(this.reader())
    return properties
}

fun String.toProperties(): Properties {
    val properties = Properties()
    properties.load(this.reader())
    return properties
}

