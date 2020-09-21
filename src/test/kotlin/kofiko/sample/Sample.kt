package kofiko.sample

import kofiko.*
import java.util.logging.Level
import java.util.logging.Logger

object DatabaseConfig {
    var user = "default_user"

    @ConfigOption(secret = true)
    var password = "changeme"

    var endpoints = listOf("http://localhost:1234")
    var unsafeSSL = false
    var dbSizeLimits = mapOf("alerts" to 50, "logs" to 200)

    init {
        Kofiko.configure(this)
    }
}

object LogConfig {
    var level = Level.INFO

    init {
        Kofiko.configure(this)
    }
}

fun main(args: Array<String>) {
    val settings = KofikoSettings(
        CliConfigProvider(args),
        EnvConfigProvider(),
        ConfigProviderIni("src/test/kotlin/kofiko/sample/config.ini"),
        JsonConfigProvider.fromFile("src/test/kotlin/kofiko/sample/config.json")
    )
    settings.onOverride = PrintOverrideNotifier()
    Kofiko.init(settings)

    Logger.getLogger("test").log(LogConfig.level, "welcome")

    connect(DatabaseConfig.endpoints, DatabaseConfig.user, DatabaseConfig.password)
}

fun connect(endpoint: List<String>, user: String, password: String) {
    // write your connection code here
}
