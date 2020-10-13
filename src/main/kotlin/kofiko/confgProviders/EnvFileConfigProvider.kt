@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

class EnvFileConfigProvider(
    configSource: ConfigSource,
    prefix: String = "",
    sectionToOptionSeparator: String = "_"
) : MapConfigProvider(
    configSource.content.toProperties().toMap(),
    prefix,
    sectionToOptionSeparator
)


private fun stripQuotes(text: String): String {
    var quoteChar = '"'
    if (text.startsWith(quoteChar) && text.endsWith(quoteChar))
        return text.substring(1, text.length - 1)

    quoteChar = '\''
    if (text.startsWith(quoteChar) && text.endsWith(quoteChar))
        return text.substring(1, text.length - 1)

    return text
}

private fun Properties.toMap(): Map<String, String> {
    return this
        .mapKeys { stripQuotes(it.key.toString()) }
        .mapValues { stripQuotes(it.value.toString()) }
}


@Suppress("unused")
class EnvFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "env")
            return EnvFileConfigProvider(ConfigSource(file))
        return null
    }
}


