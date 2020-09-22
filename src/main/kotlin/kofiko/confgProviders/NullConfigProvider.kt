@file:Suppress("PackageDirectoryMismatch")

package kofiko

import java.lang.reflect.Type

class NullConfigProvider() : KofikoConfigProvider {
    override fun read(section: String, option: String, type: Type): Any? {
        return null
    }

}