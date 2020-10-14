# kofiko - Code-First Configuration library for Kotlin

### Overview

*kofiko* = (Ko)de-(Fi)rst (Ko)nfiguration

Note: This is work-in-progress. Official first version not released yet. 

![](docs/kofiko-kotlin.png)

* Lightweight, simple and minimal boilerplate *configuration library* for Kotlin.  
* Supported formats: `.json`, `.ini`, `.properties`, `.env`
* Layered (cascading) and extensible design allows overriding the configuration from 
environment variables, command-line arguments, Java system properties (`-D`), Java Maps
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
    .addFiles(
        "sample_config.json", "sample_config.ini",
        "sample_config.env", "sample_config.properties"
    )

// optional setting to print config options with non-default value
settings.onOverride = PrintOverrideNotifier()  
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

### In-Depth

##### Registering configuration objects

`Kofiko.configure(configObj)` shall be called in order to register `configObj`. 
If Kofiko is already initialized, this call will override configuration options in the object 
immediately, otherwise configuration options will be override when `Kofiko.init()` is called.  
For singletons (Kotlin `object`), `configure()` call can be embedded in the object `init` block, e.g
`init { Kofiko.configure(this) }`.

If you don't want to use singletons (for examples if you want more unit-test flexibility),
your configuration consumer can accept the configuration section in its constructor. You can also
combine both approaches by providing a default instance in the constructor, for example:

```kotlin
class DatabaseConfig {
    var host = "default"
    var port = 8080

    companion object {
        val instance = DatabaseConfig()

        init {
            Kofiko.configure(instance)
        }
    }
}

class DatabaseConnection(databaseConfig: DatabaseConfig = DatabaseConfig.instance) {
// ..
}
``` 

##### Types support

Configuration options (fields) can be of the following types:

* String 
* Numbers (Int, Long, Double, Float, Byte, Short)
* `Boolean` (parses any casing of `true/false; 1/0; on/off; yes/no; t/f, y/n`)
* `List` (parses concise format `item1,item2,item3`)
* `Map` (parses concise format `key1:val1,key2:val2`)
* `Set` (parses concise format `item1,item2,item3`)
* `java.util.logging.Level`  
* Any type supported by [`JsonDeserializer`](https://www.javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/com/fasterxml/jackson/databind/JsonDeserializer.html),
for example: `BigInteger`, `java.util.Date`, `UUID`, ...   
Note that you can customize the expected date format by: `settings.objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")`.
* Composite objects can be provided in json format even in other config providers, for example in .ini file:
    ```kotlin
    class Person {
        var name: String = ""
        var age: Int = 0
    }

    class Credits {
        var author = Person()
    }  
    ```
    ```
    [CREDITS]
    author={ "name": "Dave", "age": 41 }
    ```   

##### Customization

Kofiko uses many conventions which can be customized by changing 
default values in `KofikoSettings` class.

* `configProviders` - List of configuration data sources. Those are evaluated 
in insertion order when looking for config option overrides.

    ```kotlin
    val settings = KofikoSettings()
    val mapper = ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    // add provider using explicit API 
    settings.configProviders.add(JsonConfigProvider(ConfigSource("config.json")))
    // or using extension method with optional init block 
    settings.addJson("config.json") { objectMapper = mapper }
    ```
  
    Many providers have specific customization options, 
    e.g `objectMapper` for `JsonConfigProvider`
    or `prefix` and `sectionToOptionSeparator` for `EnvConfigProvider` that will define
    how to resolve config options in env keys (`$prefix_$section_$option`).
    
    You can also use the `addFiles(vararg filenames: String)` extension function which adds 
    an appropriate ConfigParser according to extension of the specified filenames. It 
    is also possible to specify a filename without extension, which will look for 
    an existing filename with any supported extension.  
    For example: `settings.addFiles("sample_config")` will look for    
    sample_config.json, sample_config.ini, sample_config.env, sample_config.properties.

* `nameLookup` - a class of interface `NameLookupProvider` which defines how 
   to map between section/option name to a keys in configuration sources.
   By default, Kofiko will try all casing styles: Original, UPPERCASE, lowercase, 
   CamelCase, snake_case, kebab-case.  
   For example, for class field `conStr`, Kofiko will attempt to resolve to: 
   conStr, CONSTR,  constr, ConStr, CON_STR, con_str, CON-STR, con-str.
   
   `sectionLookupDeleteTerms` option defines list of strings to omit when 
   trying to resolve from section name from class name. By default terms are
   `"Config", "Settings", "Cfg", "Section"`. For example:
   if the class name is `DatabaseConfig`, Kofiko will try also to look 
   for a section named `Database`.
        
    ```kotlin
    settings.nameLookup.allowKebabLower = false
    settings.nameLookup.allowUpper = false
    settings.nameLookup.sectionLookupDeleteTerms.add("Options")
    ```
  
* `listSeparator` - defines the separator string between items 
  when parsing `List`, `Map`, `Set` data types. 
  Default is `,`. For example: `red,green,orange`.  
  
* `keyToValSeparator` - defines the separator string between key and value 
  when parsing `Map` data type. 
  Default is `:`. For example: `logs:100,alerts:200`.  
  
* `appendToLists` and `appendToSets` - For `List` and `Set` types, whether to append to default list value or 
  replace entire list. Default is false (replace).  

* `appendToDicts` - For `Map` type, whether to append/overwrite default map keys value or 
  replace entire map. Default is true (append/overwrite).
  
* `clearContainerPrefix` - Regardless of the `appendTo*` setting, when 
  override string is prefixed by this string, Kofiko will clear the container before
  adding new elements. Default prefix is `^C|`. For example `"^C|item1,item2"`.      

* `appendContainerPrefix` - Regardless of the `appendTo*` setting, when 
  override string is prefixed by this string, Kofiko will append/overwrite elements in 
  the container. Default prefix is `^A|`. For example `"^A|key1:val1,key2:val2"`.

* `booleanTrueStates` and `booleanFalseStates` - When parsing `Boolean` type, defines 
  the strings that will be parsed to true/false. Case insensitive.        

* `textParsers` - List of objects of interface `TextParser`. Those will be invoked 
in the insertion order when trying to parse a string into a typed field. 
 
* `objectMapper` - The `ObjectMapper` that will be used by `JsonParser` when trying to 
  parse strings to typed fields.
  
* `onOverride` - an action to perform when a field value is changed from default value. 
  Default action does nothing. It is possible to set to 
  `PrintOverrideNotifier` or `LogOverrideNotifier` in order to print message to stdout/log 
  respectively.         


##### Profiles support

You can define more than one set of hard-coded defaults using the profiles feature:

```kotlin
class DatabaseConfig: ProfileSupport  {
    var url = "default"
    var port = 5000

    override fun setProfile(profileName: String) {
        if (profileName == "dev") {
            url = "http://dev.mydb"
            port = 5002
            return
        }
        if (profileName == "prod") {
            url = "http://prod.mydb"
            port = 5003
            return
        }
        if (profileName == "staging") {
            url = "http://staging.mydb"
            port = 5004
            return
        }
    }
}

val dbCfg = DatabaseConfig()
Kofiko.configure(dbCfg)
Kofiko.init(KofikoSettings(), profileName = "staging")
print(dbCfg.url)
```

##### Extending Kofiko

* You can implement `KofikoConfigProvider` to supply a new type of config source, 
  for example `YamlConfigProvider`. If config provider support files, 
  you can also provide `FileProviderFactory` implementation to create 
  your provider from a filename (don't forget to add it to 
  `resources/META-INF/services/kofiko.FileProviderFactory` in your package so 
  that ServiceLocator will be able to locate this factory.)  

* You can implement `TextParser` to be able to parse a new data type from string.
  The parser instance is enabled by adding it to `KofikoSettings.textParsers`. 

* You can implement `NameLookupProvider` to provide custom translation from class & 
  field names to keys to look for in config sources. 
  The provider is enabled by setting `KofikoSettings.nameLookup` to this instance.  


### Installation

Kofiko is available as a package, hosted at 
the [jcenter](https://bintray.com/davidohana/kofiko/org.davidoh.kofiko.kofiko-core) repository.

[ ![Download](https://api.bintray.com/packages/davidohana/kofiko/org.davidoh.kofiko.kofiko-core/images/download.svg) ](https://bintray.com/davidohana/kofiko/org.davidoh.kofiko.kofiko-core/_latestVersion)

#### For Maven:

```xml
<!-- Add jcenter as a repository for dependencies --> 
<project>
    <repositories>
        <repository>
            <id>jcenter</id>
            <url>https://jcenter.bintray.com/</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.davidoh.kofiko</groupId>
            <artifactId>kofiko-core</artifactId>
            <version>$version</version>
        </dependency>
    </dependencies>
</project>
```

#### For Gradle:

```groovy

// Add jcenter as a repository for dependencies
repositories {
    jcenter()
}

dependencies {
    implementation 'org.davidoh.kofiko:kofiko-core:$version'
}
```

### to-do:

* Support additional file formats
  
### License: 

Apache-2.0

### Contributing 

This project welcomes external contributions. 
Please refer to [CONTRIBUTING.md](CONTRIBUTING.md) for further details.
