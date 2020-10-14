@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

class CliConfigProvider(
    var args: Array<String>,
    var overrideToken: String = "-ov",
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

fun KofikoSettings.addCli(args: Array<String>, init: CliConfigProvider.() -> Unit = {}) = this.apply {
    val provider = CliConfigProvider(args)
    provider.init()
    this.configProviders.add(provider)
}
