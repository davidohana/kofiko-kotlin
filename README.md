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
* Only 3rd-party dependency required is [`com.fasterxml.jackson.core`](https://github.com/FasterXML/jackson). 

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

#### Override default values at run-time: 

For example, from a JSON file:

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

or using *env. vars*:

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

#### Initialize Kofiko with the desired configuration sources:

```kotlin
val settings = KofikoSettings()
        .addCli(args) { this.overrideToken = "-o" }
        .addEnv()
        .addSystemProperties()
        .addFiles("sample_config.json", 
"sample_config.ini", "sample_config.env", "sample_config.properties")

    settings.onOverride = PrintOverrideNotifier()  // optional setting to print config settings with non-default value
    Kofiko.init(settings)

    // configuration is ready to use
    Logger.getLogger("test").log(LogConfig.level, "Hello Kofiko")
    println("Database user is " + DatabaseConfig.user)
```

Program output: 
```
LogConfig.level was changed from <INFO> to <WARNING> by IniConfigProvider
WARNING: Hello Kofiko
DatabaseConfig.user was changed from <default_user> to <davidoh> by IniConfigProvider
DatabaseConfig.password was changed from <[hidden]> to <[hidden]> by IniConfigProvider
DatabaseConfig.endpoints was changed from <[http://localhost:1234]> to <[prod1, prod2]> by IniConfigProvider
DatabaseConfig.dbSizeLimits was changed from <{alerts=50, logs=200}> to <{alerts=2, logs=1}> by JsonConfigProvider
Database user is davidoh
```

Kofiko can print/log the effective configuration overrides, omitting secret info like passwords.   
