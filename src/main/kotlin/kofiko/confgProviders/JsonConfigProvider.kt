@file:Suppress("PackageDirectoryMismatch")

package kofiko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import java.io.File
import java.lang.reflect.Type


/**
 * Retrieves configuration overrides from an .json file.
 * Section is expected to be a block with key under document root.
 * Option name is expected to be a key=value entry inside that block.
 *
 * For example:
 * {
 *      "section_name": {
 *          "option_name": "value"
 *      }
 * }
 * *
 */
class JsonConfigProvider(
    /**
     * Json Source
     */
    val jsonNode: JsonNode,

    /**
     * [ObjectMapper] instance to use when parsing json content.
     */
    var objectMapper: ObjectMapper = ObjectMapper()
) : KofikoConfigProvider {

    constructor(configSource: ConfigSource, objectMapper: ObjectMapper = ObjectMapper()) : this(
        objectMapper.readTree(configSource.content), objectMapper
    )

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
}

@Suppress("unused")
class JsonFileProviderFactory : FileProviderFactory {
    override fun createConfigProvider(filename: String): KofikoConfigProvider? {
        var fn = filename
        val ext = ".json"
        if (!fn.toLowerCase().endsWith(ext))
            fn = "$fn$ext"
        if (!File(fn).exists())
            return null
        return JsonConfigProvider(ConfigSource(fn))
    }
}


/**
 * Add a json file as a config provider.
 * Use the optional init block to customize options fluently.
 */
fun KofikoSettings.addJson(filename: String, init: JsonConfigProvider.() -> Unit = {}) = this.apply {
    val provider = JsonConfigProvider(ConfigSource(filename))
    provider.init()
    this.configProviders.add(provider)
}
