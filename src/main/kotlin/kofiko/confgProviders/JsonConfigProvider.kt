@file:Suppress("PackageDirectoryMismatch")

package kofiko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.File
import java.lang.reflect.Type

class JsonSource(val objectMapper: ObjectMapper = ObjectMapper()) {
    lateinit var jsonNode: JsonNode

    fun node(jsonNode: JsonNode): JsonSource {
        this.jsonNode = jsonNode
        return this
    }

    fun content(content: String): JsonSource {
        this.jsonNode = objectMapper.readTree(content)
        return this
    }

    fun file(file: File, mustExist: Boolean = false): JsonSource {
        if (!mustExist && !file.exists())
            this.jsonNode = NullNode.instance
        this.jsonNode = objectMapper.readTree(file)
        return this
    }

    fun filename(name: String, mustExist: Boolean = false): JsonSource {
        return file(File(name), mustExist)
    }

    fun checkContentExist() {
        if (!this::jsonNode.isInitialized)
            throw IllegalStateException("Content was not set. Probably forgot to call one of the content input functions.")
    }
}

class JsonConfigProvider(
    val source: JsonSource
) : KofikoConfigProvider {

    init {
        source.checkContentExist()
    }

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionNode = source.jsonNode.get(section) ?: return null
        val optionNode = sectionNode.get(option) ?: return null
        val javaType = TypeFactory.defaultInstance().constructType(type)
        return source.objectMapper.readerFor(javaType).readValue(optionNode)
    }
}

@Suppress("unused")
class JsonFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "json") {
            return JsonConfigProvider(JsonSource().file(file))
        }
        return null
    }
}


fun KofikoSettings.add(
    jsonSource: JsonSource
): KofikoSettings {
    this.configProviders.add(JsonConfigProvider(jsonSource))
    return this
}
