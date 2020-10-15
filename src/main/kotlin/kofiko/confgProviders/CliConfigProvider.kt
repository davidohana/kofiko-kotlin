@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

/**
 * Retrieves configuration overrides from command line arguments.
 *
 * By default, an override should look like "-ov section_option=value"
 */
class CliConfigProvider(
    /**
     * Command line arguments what wre specified in main())
     */
    var args: Array<String>,

    /**
     * Token used to identify configuration override argument. Override itself should
     * be in the format section_option=value after the override token, e.g "-ov database_host=localhost"
     */
    var overrideToken: String = "-ov",

    /**
     * String that separates between section name and option name when looking up for config overrides.
     */
    var sectionToOptionSeparator: String = "_",
) : KofikoConfigProvider {

    lateinit var overrides: Map<String, String>

    private fun parse(args: Array<String>, overrideToken: String): Map<String, String> {
        return args.toList()
            .zipWithNext()
            .filter { it.first == overrideToken }
            .filter { it.second.contains("=") }
            .map { it.second.split("=", limit = 2) }
            .map { Pair(it.first(), it.last()) }
            .toMap()
    }

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        if (!this::overrides.isInitialized)
            overrides = parse(args, overrideToken)

        val key = section + sectionToOptionSeparator + option
        return overrides[key]
    }
}

/**
 * Adds command line arguments as config provider. Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addCli(args: Array<String>, init: CliConfigProvider.() -> Unit = {}) = this.apply {
    val provider = CliConfigProvider(args)
    provider.init()
    this.configProviders.add(provider)
}
