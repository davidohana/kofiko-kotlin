@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.util.*


fun getProvider(file: File): KofikoConfigProvider {
    val serviceLoader = ServiceLoader.load(FileProviderFactory::class.java)
    val factories = serviceLoader.toList()

    return factories
        .asSequence()
        .mapNotNull { it.createConfigProvider(file) }
        .firstOrNull()
        ?: throw NotImplementedError("File $file cannot be handled by any provider")
}


fun KofikoSettings.addFiles(
    vararg files: File, mustExist: Boolean = false
): KofikoSettings {
    for (file in files) {
        if (!mustExist && !file.exists())
            continue

        val provider = getProvider(file)
        this.configProviders.add(provider)
    }

    return this
}
