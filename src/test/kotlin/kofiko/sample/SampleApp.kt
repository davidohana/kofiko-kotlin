package kofiko.sample

import kofiko.*
import java.util.logging.Level
import java.util.logging.Logger

data class LogConfig(
    val level: Level = Level.INFO,
    val logToFile: Boolean = true
)

object DatabaseConfig {
    var user = "default_user"

    @Secret
    var password = "changeme"

    var endpoints = listOf("http://localhost:1234")
    var unsafeSSL = false
    var dbSizeLimits = mapOf("alerts" to 50, "logs" to 200)

    init {
        Kofiko.add(this)
    }
}

fun main(args: Array<String>) {

    val settings = KofikoSettings()
        .addCli(args) { this.overrideToken = "-o" }
        .addEnv()
        .addSystemProperties()
        .addFiles(
            "sample_config.json", "sample_config.ini",
            "sample_config.env", "sample_config.properties"
        )

    // optional setting to print config options with non-default value
    settings.onOverride = PrintOverrideNotifier()

    settings.nameLookup.allowKebabLower = false
    settings.nameLookup.allowUpper = false
    settings.nameLookup.sectionLookupDeleteTerms.add("Options")

    val logConfig = LogConfig()
    Kofiko.add(logConfig)

    Kofiko.init(settings)

    // configuration is ready to use
    Logger.getLogger("test").log(logConfig.level, "Hello Kofiko")
    println("Database user is " + DatabaseConfig.user)
}
