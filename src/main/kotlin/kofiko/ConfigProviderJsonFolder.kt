package kofiko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

class ConfigProviderJsonFolder(
    val folder: Path,
    val objectMapper: ObjectMapper = ObjectMapper()
) : KofikoConfigProvider {

    private val sectionNodeCache = ConcurrentHashMap<String, JsonNode>()

    constructor(
        folderPath: String,
        objectMapper: ObjectMapper = ObjectMapper()
    ) : this(Paths.get(folderPath), objectMapper)

    fun readSectionNode(section: String): JsonNode {
        val jsonFile = folder.resolve("$section.json").toFile()
        if (!jsonFile.exists())
            return NullNode.instance
        return try {
            objectMapper.readTree(jsonFile)!!
        } catch (ex: FileNotFoundException) {
            NullNode.instance
        }
    }

    fun getSectionNode(section: String): JsonNode {
        val jsonNode = sectionNodeCache[section]
        if (jsonNode != null)
            return jsonNode
        val loadedJsonNode = readSectionNode(section)
        sectionNodeCache[section] = loadedJsonNode
        return loadedJsonNode
    }

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionNode = getSectionNode(section.toLowerCase())
        val optionNode = sectionNode.get(option) ?: return null
        val javaType = TypeFactory.defaultInstance().constructType(type)
        return objectMapper.readerFor(javaType).readValue(optionNode)
    }
}