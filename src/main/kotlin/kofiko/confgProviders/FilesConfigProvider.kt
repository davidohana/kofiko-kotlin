@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.nio.file.Path
import java.util.*


fun createConfigProvider(filename: String): KofikoConfigProvider? {
    val serviceLoader = ServiceLoader.load(FileProviderFactory::class.java)
    val factories = serviceLoader.toList()

    return factories
        .asSequence()
        .mapNotNull { it.createConfigProvider(filename) }
        .firstOrNull()
}


/**
 * Adds a list of filenames as a config providers.
 *
 * A config provider will be created for each supported file according to its name (and extension typically).
 * First factory that supports that file type wins.
 *
 * A provider will not be created for missing files.
 *
 * Its also possible to specify filename without extension. This way, each factory will attempt to add
 * an extension and check if such a file exists.
 */
fun KofikoSettings.addFiles(vararg filenames: String) = apply {
    for (filename in filenames) {
        val provider = createConfigProvider(filename)
        if (provider != null)
            this.configProviders.add(provider)
    }
}

/**
 * Adds a list of files as a config providers.
 */
fun KofikoSettings.addFiles(vararg files: File) = apply {
    val filenames = files.map { it.toString() }.toTypedArray()
    addFiles(*filenames)
}

/**
 * Adds a list of file paths as a config providers.
 */
fun KofikoSettings.addFiles(vararg paths: Path) = apply {
    val filenames = paths.map { it.toString() }.toTypedArray()
    addFiles(*filenames)
    return this
}
