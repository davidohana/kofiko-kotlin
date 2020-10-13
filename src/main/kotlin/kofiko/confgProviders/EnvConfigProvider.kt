@file:Suppress("PackageDirectoryMismatch")

package kofiko

class EnvConfigProvider(
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
    env: Map<String, String> = System.getenv()!!
) : MapConfigProvider(
    env, prefix, sectionToOptionSeparator)
