@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

class ConfigSource private constructor(val content: String) {

    constructor(file: File, mustExist: Boolean = false) : this(readFile(file, mustExist))

    constructor(filename: String, mustExist: Boolean = false) : this(File(filename), mustExist)

    constructor(path: Path, mustExist: Boolean = false) : this(path.toFile(), mustExist)

    companion object {
        fun fromText(rawText: String): ConfigSource {
            return ConfigSource(rawText)
        }
    }
}

private fun readFile(file: File, mustExist: Boolean = false): String {
    return try {
        file.readText()
    } catch (ex: FileNotFoundException) {
        if (mustExist)
            throw ex
        ""
    }
}
