# kofiko - Code-First Configuration library for Kotlin

### Overview

*kofiko* = (Ko)de-(Fi)rst (Ko)nfiguration

![](docs/kofiko-kotlin.png)

Lightweight and really simple to use configuration library for Kotlin.

##### Define application configuration as Kotlin classes/objects:

``` kotlin
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
```

Config sections are separated to classes so that configuration consumers may 
receive only the configuration they are interested it.
   
Kofiko supports configuring static Kotlin objects for easier access by configuration consumers. 
Instances of configuration classes may be configured as well.


##### Override default values from cascading sources: 

* `.ini` files
* `.json` files
* `.properties` files
* `.env` files
* Environment variables
* Command-line arguments
* System Properties

For example:

```shell script
database_user=davidoh 
database_password=reallysecret! 
database_endpoints=prod1,prod2
log_level=WARNING
```

or:

``` json
{
  "database": {
    "db_size_limits": {
      "logs": 1,
      "alerts": 2
    }
  }
}
```

Kofiko uses out-of-the-box configurable conventions to search for matching configuration entries, 
looking for lowercase, uppercase, camel-case, snake-case, kebab-case matches.   

##### Initialize Kofiko with the desired configuration sources

```kotlin
val settings = KofikoSettings(
        ConfigProviderCli(args),
        ConfigProviderEnv(),
        ConfigProviderIni("config.ini"),
        ConfigProviderJson.fromFile("config.json")
    )
settings.onOverride = PrintOverrideNotifier()
Kofiko.init(settings)

// use configuration
Logger.getLogger("test").log(LogConfig.level, "welcome")
connect(DatabaseConfig.endpoints, DatabaseConfig.user, DatabaseConfig.password)
```

Kofiko can print/log the effective configuration overrides, omitting secrets like passwords.   
```
WARNING: welcome
DatabaseConfig.user was changed from <default_user> to <davidoh> by ConfigProviderIni
DatabaseConfig.password was changed from <[hidden]> to <[hidden]> by ConfigProviderIni
DatabaseConfig.endpoints was changed from <[http://localhost:1234]> to <[prod1, prod2]> by ConfigProviderIni
DatabaseConfig.dbSizeLimits was changed from <{alerts=50, logs=200}> to <{alerts=2, logs=1}> by ConfigProviderJson
```




