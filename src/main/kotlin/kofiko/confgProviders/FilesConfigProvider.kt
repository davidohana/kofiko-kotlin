@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.lang.reflect.Type
import java.util.*

class FilesConfigProvider(vararg files: File) : KofikoConfigProvider {
    constructor(vararg filenames: String) : this(*filenames.map { File(it) }.toTypedArray())

    val providers = files.map { getProvider(it) }

    fun getProvider(file: File): KofikoConfigProvider {
        if (!file.exists())
            return NullConfigProvider()

        val serviceLoader = ServiceLoader.load(FileProviderFactory::class.java)
        val factories = serviceLoader.toList()

        return factories
            .asSequence()
            .mapNotNull { it.createConfigProvider(file) }
            .firstOrNull()
            ?: throw NotImplementedError("File $file cannot be handled by any provider")
    }

    override fun read(section: String, option: String, type: Type): Any? {
        return providers.asSequence()
            .mapNotNull { it.read(section, option, type) }
            .firstOrNull()
    }
}

