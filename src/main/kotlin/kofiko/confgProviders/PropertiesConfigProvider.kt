@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

class PropertiesConfigProvider(
    properties: Properties = System.getProperties(),
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


@Suppress("unused")
class PropertiesFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(filename: String): KofikoConfigProvider? {
        var fn = filename
        val ext = ".properties"
        if (!fn.toLowerCase().endsWith(ext) && !fn.toLowerCase().endsWith(".props"))
            fn = "$fn$ext"
        if (!File(fn).exists())
            return null
        return PropertiesConfigProvider(ConfigSource(fn))
    }
}


fun KofikoSettings.addProperties(filename: String, init: PropertiesConfigProvider.() -> Unit = {}) = this.apply {
    val provider = PropertiesConfigProvider(File(filename).loadProperties())
    provider.init()
    this.configProviders.add(provider)
}

fun KofikoSettings.addSystemProperties(init: PropertiesConfigProvider.() -> Unit = {}) = this.apply {
    val provider = PropertiesConfigProvider()
    provider.init()
    this.configProviders.add(provider)
}

