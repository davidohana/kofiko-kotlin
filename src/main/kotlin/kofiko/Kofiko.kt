@file:Suppress("UnnecessaryVariable")

package kofiko

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Code-First Configuration Manager for Kotlin.
 * Overwrites default property values in configuration classes from a list of specified configuration providers.
 * Can be used as a singleton [Kofiko.instance] or by creating an instance of this class.
 * Configuration objects (sections) can be registered using [Kofiko.add] before or after [Kofiko.init] call.
 * [Kofiko.init] shall be called with a [KofikoSettings] which contains at least one [KofikoConfigProvider].
 * During the init call, all configuration classes that where already registered with [Kofiko.add] are
 * processed - default values are overridden using config providers in the insertion order.
 * Configuration objects that are added after init are configured immediately.
 */
class Kofiko {
    /**
     * Configuration settings that are being used to configure objects.
     */
    lateinit var settings: KofikoSettings
        private set

    /**
     * Active profile (optional). Used for selecting a set of hard coded default configuration for configuration
     * classes that implement [ProfileSupport].
     */
    lateinit var profileName: String
        private set

    private val registeredConfigSections = CopyOnWriteArraySet<Any>()

    private val _sectionNameToOverrides = mutableMapOf<String, List<FieldOverride>>()

    /**
     * Contains all fields that were changed from default values so far, grouped by section name.
     */
    val sectionNameToOverrides: Map<String, List<FieldOverride>>
        get() = _sectionNameToOverrides

    private constructor()

    /**
     * Create a new instance of the configuration manager.
     *
     * @param settings Contains customization options. Typically iy should contain at least one [KofikoConfigProvider].
     *
     * @param profileName Optional name of a profile that will be used for selecting a set of
     * hard-coded default configuration for configuration classes that implements [ProfileSupport]. Default profile
     * is an empty string.
     */
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
            var shallAppend = settings.appendToMaps
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

        if (oldValue is Set<*>) {
            val newSet = (newValue as Set<*>).toMutableSet()

            val mergedSet = oldValue.toMutableSet()
            var shallAppend = settings.appendToSets
            if (newSet.contains(settings.clearContainerPrefix)) {
                shallAppend = false
                newSet.remove(settings.clearContainerPrefix)
            }
            if (newSet.contains(settings.appendContainerPrefix)) {
                shallAppend = true
                newSet.remove(settings.appendContainerPrefix)
            }

            if (!shallAppend)
                mergedSet.clear()

            mergedSet.addAll(newSet)
            return mergedSet
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
                        newValue = parseText(settings.textParsers, newValue, field.genericType)
                    }
                    val oldValue = field.get(configObject)
                    val mergedValue = mergeContainers(oldValue, newValue)
                    if (Modifier.isStatic(field.modifiers) && Modifier.isFinal(field.modifiers)) {
                        throw IllegalAccessException(
                            "Cannot configure field '${field.name}'. Configuring static final fields or val properties in Kotlin objects is not supported.")
                    }
                    else
                        field.set(configObject, mergedValue)
                    return Pair(newValue, provider)
                }
            }
        }
        return null
    }

    private fun overrideConfigSection(configSection: Any): List<FieldOverride> {
        val fields = getOverridableFields(configSection.javaClass, settings.configureReadonlyProperties)

        val oldValues = fields.map { it.get(configSection) }
        val sectionOverrides = mutableListOf<FieldOverride>()

        if (configSection is ProfileSupport)
            configSection.setProfile(profileName)

        val sectionName = getSectionName(configSection)
        val sectionLookups = settings.nameLookup.getSectionLookups(sectionName)
        for ((i, field) in fields.withIndex()) {
            val optionName = getOptionName(field)
            var oldValue = oldValues[i]

            val optionLookups = settings.nameLookup.getOptionLookups(optionName)
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

    /**
     * Registers and configures a new configuration object instance as a section.
     *
     * Configuration object can be any object. Only public read-write (var) properties and fields will be configured.
     *
     * @return a list of fields that were changed from default value.
     */
    fun add(configSection: Any): List<FieldOverride> {
        registeredConfigSections.add(configSection)
        if (this::settings.isInitialized)
            return overrideConfigSection(configSection)
        return emptyList()
    }

    /**
     * Returns a list of all registered configuration objects.
     */
    fun getRegisteredSections(): List<Any> {
        return registeredConfigSections.toList()
    }

    companion object {
        /**
         * Default instance of the configuration manager.
         *
         * You should typically use this instance unless you need multiple configuration managers.
         * Note that [init] and [add] calls works on this [instance].
         */
        val instance = Kofiko()

        /**
         * Initializes the default configuration manager and configures all registered configuration objects.
         *
         * @param settings Contains customization options. Typically iy should contain at least one [KofikoConfigProvider].
         * @param profileName Optional name of a profile that will be used for selecting a set of
         * hard-coded default configuration for configuration classes that implements [ProfileSupport]. Default profile
         * is an empty string.
         */
        fun init(settings: KofikoSettings, profileName: String = "") {
            instance.initSettings(settings, profileName)
        }


        /**
         * Registers a configuration object with the default configuration manager.
         * Configuration object can be any object. Only public read-write (var) properties and fields will be configured.
         *
         * If configuration manager was already initialized with [init], the section will be configured immediately.
         * Otherwise it will be configured once init is called.
         *
         * @return a list of fields that were changed from default value. Empty list in case [init] was not called yet.
         */
        fun add(configSection: Any): List<FieldOverride> {
            return instance.add(configSection)
        }
    }
}

/**
 * An [OverrideNotifier] that print field configuration changes to the standard output stream.
 */
class PrintOverrideNotifier : OverrideNotifier {
    override fun accept(override: FieldOverride) {
        println(override.toString())
    }
}

/**
 * An [OverrideNotifier] that print field configuration changes to log using [java.util.logging.Logger]
 *
 * @param logLevel The desired logging level of override notifications. Default is [java.util.logging.Level.INFO]
 */
class LogOverrideNotifier(val logLevel: Level = Level.INFO) : OverrideNotifier {
    private val logger = Logger.getLogger(Kofiko::class.java.name)
    override fun accept(override: FieldOverride) {
        logger.log(logLevel, override.toString())
    }
}

