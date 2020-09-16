package kofiko

import java.io.File

class ConfigProviderEnvFile(
    envFile: File,
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) :
    ConfigProviderMap(
        envFile.loadProperties()
            .mapKeys { it.key.toString() }
            .mapValues { it.value.toString() },
        prefix,
        sectionToOptionSeparator,
        trimWhitespace = true,
        trimQuotes = true
    )
