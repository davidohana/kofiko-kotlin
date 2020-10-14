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


fun KofikoSettings.addFiles(vararg filenames: String) = apply {
    for (filename in filenames) {
        val provider = createConfigProvider(filename)
        if (provider != null)
            this.configProviders.add(provider)
    }
}

fun KofikoSettings.addFiles(vararg files: File) = apply {
    val filenames = files.map { it.toString() }.toTypedArray()
    addFiles(*filenames)
}

fun KofikoSettings.addFiles(vararg paths: Path) = apply {
    val filenames = paths.map { it.toString() }.toTypedArray()
    addFiles(*filenames)
    return this
}
