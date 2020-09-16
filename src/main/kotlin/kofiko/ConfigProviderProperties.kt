package kofiko

import java.io.File
import java.util.*

open class ConfigProviderProperties(
    properties: Properties,
    prefix: String = "",
    sectionToOptionSeparator: String = ".",
) : ConfigProviderMap(
    properties
        .mapKeys { it.key.toString() }
        .mapValues { it.value.toString() },
    prefix,
    sectionToOptionSeparator,
    trimWhitespace = true,
    trimQuotes = false
)

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

