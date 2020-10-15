@file:Suppress("PackageDirectoryMismatch")

package kofiko

/**
 * Retrieves configuration overrides from environment variables.
 * By default, expected env var should be in the format section_option=value or prefix_section_option=key
 * if [keyPrefix] is set.
 */
class EnvConfigProvider(
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
    sectionToOptionSeparator: String = "_",

    /**
     * Environment variables source, by default [System.getenv]
     */
    env: Map<String, String> = System.getenv()!!

) : MapConfigProvider(
    env, keyPrefix, sectionToOptionSeparator
)


/**
 * Add System environment variables as a config provider.
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addEnv(init: EnvConfigProvider.() -> Unit = {}) = this.apply {
    val provider = EnvConfigProvider()
    provider.init()
    this.configProviders.add(provider)
}

