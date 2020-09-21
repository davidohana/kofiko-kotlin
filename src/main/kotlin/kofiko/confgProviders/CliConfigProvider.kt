@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

class CliConfigProvider(
    val args: Array<String>,
    val overrideToken: String = "-ov",
    val sectionToOptionSeparator: String = "_",
) : KofikoConfigProvider {

    val overrides = parse(args, overrideToken)

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
        val key = section + sectionToOptionSeparator + option
        return overrides[key]
    }
}