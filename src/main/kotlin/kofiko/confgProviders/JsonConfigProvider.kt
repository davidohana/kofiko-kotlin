@file:Suppress("PackageDirectoryMismatch")

package kofiko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.File
import java.lang.reflect.Type

class JsonConfigProvider(
    val jsonNode: JsonNode,
    val objectMapper: ObjectMapper = ObjectMapper()
) : KofikoConfigProvider {

    constructor(jsonFile: File, objectMapper: ObjectMapper = ObjectMapper())
            : this(fileToNode(jsonFile, objectMapper))

    constructor(jsonFileName: String, objectMapper: ObjectMapper = ObjectMapper())
            : this(fileToNode(File(jsonFileName), objectMapper))


    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionNode = jsonNode.get(section) ?: return null
        val optionNode = sectionNode.get(option) ?: return null
        val javaType = TypeFactory.defaultInstance().constructType(type)
        return objectMapper.readerFor(javaType).readValue(optionNode)
    }

    companion object {
        fun fromString(json: String, objectMapper: ObjectMapper = ObjectMapper()): JsonConfigProvider {
            return JsonConfigProvider(objectMapper.readTree(json), objectMapper)
        }
    }

}

internal fun fileToNode(jsonFile: File, objectMapper: ObjectMapper): JsonNode {
    if (!jsonFile.exists())
        return NullNode.instance

    return objectMapper.readTree(jsonFile)
}

@Suppress("unused")
class JsonFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(file: File): KofikoConfigProvider? {
        if (file.extension.toLowerCase() == "json")
            return JsonConfigProvider(file)
        return null
    }
}