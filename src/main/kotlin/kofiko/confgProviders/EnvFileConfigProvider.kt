@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

class EnvFileConfigProvider(
    configSource: ConfigSource,
    keyPrefix: String = "",
    sectionToOptionSeparator: String = "_"
) : MapConfigProvider(
    configSource.content.toProperties().toMap(),
    keyPrefix,
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
    override fun createConfigProvider(filename: String): KofikoConfigProvider? {
        var fn = filename
        val ext = ".env"
        if (!fn.toLowerCase().endsWith(ext))
            fn = "$fn$ext"
        if (!File(fn).exists())
            return null
        return EnvFileConfigProvider(ConfigSource(fn))
    }
}


fun KofikoSettings.addEnvFile(filename: String, init: EnvFileConfigProvider.() -> Unit = {}) = this.apply {
    val provider = EnvFileConfigProvider(ConfigSource(filename))
    provider.init()
    this.configProviders.add(provider)
}

