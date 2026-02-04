package re.neotamia.config.json

import re.neotamia.config.NTConfig
import re.neotamia.nightconfig.json.JsonFormat

/**
 * Registers the default JSON format with default extensions.
 */
fun NTConfig.registerJson() {
    JsonModule.register(this)
}

/**
 * Registers a custom JSON format and extensions.
 *
 * If no extensions are provided, the default extensions are used.
 */
fun NTConfig.registerJson(format: JsonFormat<*>, vararg extensions: String) {
    if (extensions.isEmpty()) {
        JsonModule.register(this, format, *JsonModule.DEFAULT_EXTENSIONS)
    } else {
        JsonModule.register(this, format, *extensions)
    }
}
