@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

class PropertiesConfigProvider(
    properties: Properties,
    prefix: String = "",
    sectionToOptionSeparator: String = ".",
) : MapConfigProvider(
    properties
        .mapKeys { it.key.toString() }
        .mapValues { it.value.toString() },
    prefix,
    sectionToOptionSeparator,
) {
    constructor(
        configSource: ConfigSource,
        prefix: String = "",
        sectionToOptionSeparator: String = "."
    ) : this(configSource.content.toProperties(), prefix, sectionToOptionSeparator)
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

