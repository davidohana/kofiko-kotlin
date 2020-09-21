@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File

class EnvFileConfigProvider(
    envFile: File,
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) :
    MapConfigProvider(
        envFile.loadProperties()
            .mapKeys { it.key.toString() }
            .mapValues { it.value.toString() },
        prefix,
        sectionToOptionSeparator,
        trimWhitespace = true,
        trimQuotes = true
    )
