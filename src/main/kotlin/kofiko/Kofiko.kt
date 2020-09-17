@file:Suppress("UnnecessaryVariable")

package kofiko

import java.lang.reflect.Field
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Level
import java.util.logging.Logger


class Kofiko {
    lateinit var settings: KofikoSettings
        private set

    lateinit var profileName: String
        private set

    private val registeredConfigSections = CopyOnWriteArrayList<Any>()

    private val _sectionNameToOverrides = mutableMapOf<String, List<FieldOverride>>()

    val sectionNameToOverrides: Map<String, List<FieldOverride>>
        get() = _sectionNameToOverrides

    private constructor()

    constructor(settings: KofikoSettings, profileName: String = "") {
        this.settings = settings
        this.profileName = profileName
    }

    private fun initSettings(settings: KofikoSettings, profileName: String) {
        this.settings = settings
        this.profileName = profileName
        registeredConfigSections.forEach { overrideConfigSection(it) }
    }

    private fun mergeContainers(oldValue: Any, newValue: Any): Any {
        if (oldValue is Map<*, *>) {
            val newMap = (newValue as Map<*, *>).toMutableMap()

            val mergedMap = oldValue.toMutableMap()
            var shallAppend = settings.appendToDicts
            if (newMap.containsKey(settings.clearContainerPrefix)) {
                shallAppend = false
                newMap.remove(settings.clearContainerPrefix)
            }
            if (newMap.containsKey(settings.appendContainerPrefix)) {
                shallAppend = true
                newMap.remove(settings.appendContainerPrefix)
            }

            if (!shallAppend)
                mergedMap.clear()

            mergedMap.putAll(newMap)
            return mergedMap
        }

        if (oldValue is List<*>) {
            val newList = (newValue as List<*>).toMutableList()

            val mergedList = oldValue.toMutableList()
            var shallAppend = settings.appendToLists
            if (newList.contains(settings.clearContainerPrefix)) {
                shallAppend = false
                newList.remove(settings.clearContainerPrefix)
            }
            if (newList.contains(settings.appendContainerPrefix)) {
                shallAppend = true
                newList.remove(settings.appendContainerPrefix)
            }

            if (!shallAppend)
                mergedList.clear()

            mergedList.addAll(newList)
            return mergedList
        }
        return newValue
    }

    private fun overrideField(
        field: Field,
        configObject: Any,
        sectionLookups: List<String>,
        optionLookups: List<String>,
        readers: List<KofikoConfigProvider>,
    ): Pair<Any, KofikoConfigProvider>? {

        for (provider in readers) {
            for (sectionName in sectionLookups) {
                for (optionName in optionLookups) {
                    var newValue = try {
                        provider.read(sectionName, optionName, field.genericType) ?: continue
                    } catch (ex: Exception) {
                        throw RuntimeException(
                            "Failed to read override value for section=$sectionName, option=$optionName by provider $provider",
                            ex
                        )
                    }

                    if (newValue is String) {
                        val textToTypeConverter = DefaultTextConverter(settings)
                        newValue = textToTypeConverter.convert(newValue, field.genericType)
                    }
                    val oldValue = field.get(configObject)
                    val mergedValue = mergeContainers(oldValue, newValue)
                    field.set(configObject, mergedValue)
                    return Pair(newValue, provider)
                }
            }
        }
        return null
    }

    private fun overrideConfigSection(configSection: Any): List<FieldOverride> {
        val fields = getOverridableFields(configSection.javaClass)

        val oldValues = fields.map { it.get(configSection) }
        val sectionOverrides = mutableListOf<FieldOverride>()

        if (configSection is ProfileSupport)
            configSection.setProfile(profileName)

        val sectionName = getSectionName(configSection)
        val sectionLookups = getSectionNameLookups(sectionName, settings)
        for ((i, field) in fields.withIndex()) {
            val optionName = getOptionName(field)
            var oldValue = oldValues[i]

            val optionLookups = getOptionNameLookups(optionName, settings)
            val overrideResult = overrideField(
                field, configSection, sectionLookups, optionLookups, settings.configProviders
            )

            var providerName = "Profile ($profileName)"
            if (overrideResult != null) {
                val provider = overrideResult.second
                providerName = provider::class.java.simpleName
            }

            var newValue = field.get(configSection)
            if (oldValue == newValue)
                continue

            // report that field value changed
            if (isSecretOption(field)) {
                val hiddenToken = "[hidden]"
                oldValue = hiddenToken
                newValue = hiddenToken
            }

            val fieldOverride = FieldOverride(field, sectionName, optionName, oldValue, newValue, providerName)
            settings.onOverride.accept(fieldOverride)
            sectionOverrides.add(fieldOverride)
        }

        _sectionNameToOverrides[sectionName] = sectionOverrides
        return sectionOverrides
    }

    fun configure(configSection: Any): List<FieldOverride> {
        registeredConfigSections.add(configSection)
        if (this::settings.isInitialized)
            return overrideConfigSection(configSection)
        return emptyList()
    }

    fun getRegisteredSections(): List<Any> {
        return registeredConfigSections.toList()
    }

    companion object {
        val instance = Kofiko()

        fun init(settings: KofikoSettings, profileName: String = "") {
            instance.initSettings(settings, profileName)
        }

        fun configure(configSection: Any) {
            instance.configure(configSection)
        }
    }
}

class PrintOverrideNotifier : OverrideNotifier {
    override fun accept(override: FieldOverride) {
        println(override.toString())
    }
}

class LogOverrideNotifier : OverrideNotifier {
    private val logger = Logger.getLogger(Kofiko::class.java.name)
    override fun accept(override: FieldOverride) {
        logger.log(Level.INFO, override.toString())
    }
}

