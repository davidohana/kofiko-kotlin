@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File

class EnvFileSource(
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) {
    lateinit var envContent: String

    fun content(content: String): EnvFileSource {
        this.envContent = content
        return this
    }

    fun file(file: File, mustExist: Boolean = false): EnvFileSource {
        if (!mustExist && !file.exists())
            this.envContent = ""
        this.envContent = file.readText()
        return this
    }

    fun filename(name: String, mustExist: Boolean = false): EnvFileSource {
        return file(File(name), mustExist)
    }

    fun checkContentExist() {
        if (!this::envContent.isInitialized)
            throw IllegalStateException("Content was not set. Probably forgot to call one of the content input functions.")
    }
}
class EnvFileConfigProvider(
    envFile: File,
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
) :
    MapConfigProvider(
        envFile.loadProperties()
            .mapKeys { it.key.toString() }
            .mapValues { it.value.toString() },
        prefix,
        sectionToOptionSeparator,
        trimWhitespace = true,
        trimQuotes = true
    )

//
//class EnvFileConfigProvider(
//   source: EnvFileSource
//) :
//    MapConfigProvider(
//        source.loadProperties()
//            .mapKeys { it.key.toString() }
//            .mapValues { it.value.toString() },
//        prefix,
//        sectionToOptionSeparator,
//        trimWhitespace = true,
//        trimQuotes = true
//    )


@Suppress("unused")
class EnvFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "env")
            return EnvFileConfigProvider(file)
        return null
    }
}


fun KofikoSettings.addEnvFile(
    file: File,
    allowMissingFile: Boolean = true,
): KofikoSettings {
    if (allowMissingFile && !file.exists())
        return this

    this.configProviders.add(EnvFileConfigProvider(file))
    return this
}

//
//fun KofikoSettings.addIni(
//    filename: String,
//    allowMissingFile: Boolean = true
//): KofikoSettings {
//    this.addIni(File(filename), allowMissingFile)
//    return this
//}