# kofiko - Code-First Configuration library for Kotlin

### Overview

*kofiko* = (Ko)de-(Fi)rst (Ko)nfiguration

Note: This is work-in-progress. Official first version not released yet. 

![](docs/kofiko-kotlin.png)

* Lightweight, simple and minimal boilerplate *configuration library* for Kotlin.  
* Supported formats: `.json`, `.ini`, `.properties`, `.env`
* Layered design allows overriding the configuration from 
environment variables, command-line arguments, Java system properties (`-D`) 
in any precedence order you like.     


#### Define application configuration as Kotlin classes/objects:

``` kotlin
class DatabaseConfig {
    var user = "default_user"

    @ConfigOption(secret = true)
    var password = "changeme"

    var endpoints = listOf("http://localhost:1234")
    var unsafeSSL = false
    var dbSizeLimits = mapOf("alerts" to 50, "logs" to 200)
}

object LogConfig {
    var level = Level.INFO

    init {
        Kofiko.configure(this)
    }
}
```

Each config section is represented by a `class` / `object` so that configuration consumers may 
receive only the configuration of interest.

Configuration options should be declared as `var` properties (read/write) with baked-in defaults.
   
By using Kotlin `object`, you may easily access configuration as a singleton without injection.    
However, instances of configuration classes may be configured as well.

#### Override default values at run time: 

For example, from JSON:

``` json
{
  "database": {
    "user": "davidoh",
    "db_size_limits": {
      "logs": 1,
      "events": 120
    }
  }
}
```

or using env. vars:

```shell script
DATABASE_user=davidoh \
DATABASE_password=reallysecret! \
DATABASE_endpoints=prod1,prod2 \
LOG_level=WARNING \
DATABASE_DB_SIZE_LIMITS=logs:5,events:120 \
java -cp my_app.jar
```

Kofiko uses out-of-the-box (configurable) conventions to search for matching configuration entries, 
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




