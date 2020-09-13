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
        typeConverter: TextToTypeConverter
    ): Any? {
        val sectionNode = jsonNodes.mapNotNull { it.get(section) }.firstOrNull() ?: return null
        val optionNode = sectionNode.get(option) ?: return null

        val javaType = TypeFactory.defaultInstance().constructType(type)
        return objectMapper.readerFor(javaType).readValue(optionNode)
    }

    companion object {
        fun fromFiles(jsonFiles: Collection<File>, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = jsonFiles.map { objectMapper.readTree(it) }
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromFile(jsonFile: File, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = listOf(objectMapper.readTree(jsonFile))
            return ConfigProviderJson(jsonNodes, objectMapper)
        }

        fun fromFile(jsonFilename: String, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            return fromFile(File(jsonFilename), objectMapper)
        }

        fun fromString(json: String, objectMapper: ObjectMapper = ObjectMapper()): ConfigProviderJson {
            val jsonNodes = listOf(objectMapper.readTree(json))
            return ConfigProviderJson(jsonNodes, objectMapper)
        }
    }
}