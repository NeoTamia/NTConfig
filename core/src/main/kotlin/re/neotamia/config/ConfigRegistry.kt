package re.neotamia.config

import re.neotamia.config.format.ConfigFormat
import java.util.logging.Logger

class ConfigRegistry {
    private val logger: Logger = Logger.getLogger(ConfigRegistry::class.java.name)
    private val formatProviders: MutableList<ConfigFormat> = mutableListOf()

    fun registerConfigFormat(provider: ConfigFormat) {
        formatProviders.add(provider)
        logger.info("Registered config format: ${provider::class.java.name}")
    }

    fun getFormatFromExtension(extension: String): ConfigFormat? {
        for (provider in formatProviders) {
            if (provider.supportedExtensions.contains(extension.lowercase()))
                return provider
        }
        logger.warning("No config format found for extension: $extension")
        return null
    }
}