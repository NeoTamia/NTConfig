package re.neotamia.config.toml

import re.neotamia.config.NTConfig
import re.neotamia.nightconfig.toml.TomlFormat

/**
 * Registers the default TOML format with default extensions.
 */
fun NTConfig.registerToml() {
    TomlModule.register(this)
}

/**
 * Registers a custom TOML format and extensions.
 *
 * If no extensions are provided, the default extensions are used.
 */
fun NTConfig.registerToml(format: TomlFormat, vararg extensions: String) {
    if (extensions.isEmpty()) {
        TomlModule.register(this, format, *TomlModule.DEFAULT_EXTENSIONS)
    } else {
        TomlModule.register(this, format, *extensions)
    }
}
