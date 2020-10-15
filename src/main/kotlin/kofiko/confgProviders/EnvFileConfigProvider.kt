@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

/**
 * Retrieves configuration overrides from environment file.
 *
 * Env file format is the same as a `.properties` file - lines of key=val, but also optional single or double quotes
 * are stripped if exist.
 *
 * By default, expected env var should be in the format section_option=value or prefix_section_option=key
 * if prefix is set.
 */
class EnvFileConfigProvider(
    /**
     * Source of env file
     */
    configSource: ConfigSource,

    /**
     * Prefix to add when searching for env vars. Default is empty.
     * Usage of a prefix is recommended (like a namespace) in order to prevent collisions with other env vars.
     *
     * For example MYAPP_database_host=localhost.
     */
    keyPrefix: String = "",

    /**
     * String that separates between section name and option name when looking up for config overrides.
     */
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


/**
 * Add a `.env` file as config provider.
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addEnvFile(filename: String, init: EnvFileConfigProvider.() -> Unit = {}) = this.apply {
    val provider = EnvFileConfigProvider(ConfigSource(filename))
    provider.init()
    this.configProviders.add(provider)
}

