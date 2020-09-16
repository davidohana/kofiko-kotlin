package kofiko

import java.io.File
import java.util.*

class ConfigProviderProperties(
    properties: Properties,
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) : ConfigProviderMap(
    prefix,
    sectionToOptionSeparator,
    properties
        .mapKeys { it.key.toString() }
        .mapValues { it.value.toString() }) {
}

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

