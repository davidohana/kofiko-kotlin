@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*

/**
 * Retrieves configuration overrides from [Properties] instance. By default - [System.getProperties] is used.
 *
 * Use extension method [loadProperties] to use a .properties file as a source.
 *
 * By default, expected peroperties should be in the format section_option=value or prefix_section_option=key
 * if [keyPrefix] is set.
 */
class PropertiesConfigProvider(
    /**
     * Properties source
     */
    properties: Properties = System.getProperties(),

    /**
     * Prefix to add when searching for keys in properties. Default is empty.
     *
     * For example MYAPP_database_host=localhost.
     */
    keyPrefix: String = "",

    /**
     * String that separates between section name and option name when looking up for config overrides.
     */
    sectionToOptionSeparator: String = ".",
) : MapConfigProvider(
    properties
        .mapKeys { it.key.toString() }
        .mapValues { it.value.toString() },
    keyPrefix,
    sectionToOptionSeparator,
) {
    constructor(
        configSource: ConfigSource,
        prefix: String = "",
        sectionToOptionSeparator: String = "."
    ) : this(configSource.content.toProperties(), prefix, sectionToOptionSeparator)
}

/**
 * Loads and creates [Properties] instance from file.
 */
fun File.loadProperties(): Properties {
    val properties = Properties()
    properties.load(this.reader())
    return properties
}

/**
 * Creates [Properties] instance from content string.
 */
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


/**
 * Add a properties file as a config provider.
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addProperties(filename: String, init: PropertiesConfigProvider.() -> Unit = {}) = this.apply {
    val provider = PropertiesConfigProvider(File(filename).loadProperties())
    provider.init()
    this.configProviders.add(provider)
}

/**
 * Add JVM system properties as a config provider.
 * Those can be specified at command line using -D argument,
 * e.g `java -Ddatabase_host=localhost -cp my_app.jar`
 *
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addSystemProperties(init: PropertiesConfigProvider.() -> Unit = {}) = this.apply {
    val provider = PropertiesConfigProvider()
    provider.init()
    this.configProviders.add(provider)
}

