package re.neotamia.config.yaml

import re.neotamia.config.NTConfig
import re.neotamia.nightconfig.yaml.YamlFormat

/**
 * Registers the default YAML format with default extensions.
 */
fun NTConfig.registerYaml() {
    YamlModule.register(this)
}

/**
 * Registers a custom YAML format and extensions.
 *
 * If no extensions are provided, the default extensions are used.
 */
fun NTConfig.registerYaml(format: YamlFormat, vararg extensions: String) {
    if (extensions.isEmpty()) {
        YamlModule.register(this, format, *YamlModule.DEFAULT_EXTENSIONS)
    } else {
        YamlModule.register(this, format, *extensions)
    }
}
