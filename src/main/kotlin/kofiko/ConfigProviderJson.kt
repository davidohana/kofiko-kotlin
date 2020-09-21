package kofiko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.File
import java.lang.reflect.Type

class ConfigProviderJson(
    val jsonNodes: Collection<JsonNode>,
    val objectMapper: ObjectMapper = ObjectMapper()
) : KofikoConfigProvider {

    override fun read(
        section: String,
        option: String,
        type: Type,
    ): Any? {
        val sectionNode = jsonNodes
            .asSequence()
            .mapNotNull { it.get(section) }
            .firstOrNull() ?: return null
        val optionNode = sectionNode.get(option) ?: return null
        val javaType = TypeFactory.defaultInstance().constructType(type)
        return objectMapper.readerFor(javaType).readValue(optionNode)
    }

    companion object {
        fun filesToNodes(jsonFiles: List<File>, objectMapper: ObjectMapper): List<JsonNode> {
            return jsonFiles.filter { it.exists() }.map { objectMapper.readTree(it) }
        }

        fun fromFiles(vararg jsonFiles: File, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = filesToNodes(jsonFiles.toList(), objectMapper)
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromFiles(vararg jsonFilenames: String, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonFiles = jsonFilenames.map { File(it) }
            val jsonNodes = filesToNodes(jsonFiles.toList(), objectMapper)
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromFile(jsonFile: File, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = filesToNodes(listOf(jsonFile), objectMapper)
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromFile(jsonFilename: String, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = filesToNodes(listOf(File(jsonFilename)), objectMapper)
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromString(json: String, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = listOf(objectMapper.readTree(json))
            return ConfigProviderJson(jsonNodes, objectMapper)
        }
    }
}