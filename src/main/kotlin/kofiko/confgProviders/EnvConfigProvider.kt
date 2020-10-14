@file:Suppress("PackageDirectoryMismatch")

package kofiko

class EnvConfigProvider(
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
    env: Map<String, String> = System.getenv()!!
) : MapConfigProvider(
    env, prefix, sectionToOptionSeparator
)


fun KofikoSettings.addEnv(init: EnvConfigProvider.() -> Unit = {}) = this.apply {
    val provider = EnvConfigProvider()
    provider.init()
    this.configProviders.add(provider)
}

