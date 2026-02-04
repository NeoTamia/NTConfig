package re.neotamia.config.yaml;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.nightconfig.yaml.YamlFormat;

/**
 * Helper methods to register the YAML format for {@link NTConfig}.
 */
public final class YamlModule {
    /**
     * Default extensions for YAML configs.
     */
    public static final String[] DEFAULT_EXTENSIONS = new String[]{"yaml", "yml"};

    /**
     * Utility class.
     */
    private YamlModule() {
    }

    /**
     * Registers the default YAML format with the default extensions.
     *
     * @param config the NTConfig instance to register on
     */
    public static void register(@NotNull NTConfig config) {
        register(config, YamlFormat.defaultInstance(), DEFAULT_EXTENSIONS.clone());
    }

    /**
     * Registers a custom YAML format and extensions.
     *
     * @param config     the NTConfig instance to register on
     * @param format     the YAML format to use
     * @param extensions the file extensions to bind
     */
    public static void register(@NotNull NTConfig config, @NotNull YamlFormat format, @NotNull String... extensions) {
        config.registerFormat(format, extensions);
    }
}
