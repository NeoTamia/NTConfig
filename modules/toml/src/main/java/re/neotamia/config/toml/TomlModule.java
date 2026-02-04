package re.neotamia.config.toml;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.nightconfig.toml.TomlFormat;

/**
 * Helper methods to register the TOML format for {@link NTConfig}.
 */
public final class TomlModule {
    /**
     * Default extensions for TOML configs.
     */
    public static final String[] DEFAULT_EXTENSIONS = new String[]{"toml"};

    /**
     * Utility class.
     */
    private TomlModule() {
    }

    /**
     * Registers the default TOML format with the default extensions.
     *
     * @param config the NTConfig instance to register on
     */
    public static void register(@NotNull NTConfig config) {
        register(config, TomlFormat.instance(), DEFAULT_EXTENSIONS.clone());
    }

    /**
     * Registers a custom TOML format and extensions.
     *
     * @param config     the NTConfig instance to register on
     * @param format     the TOML format to use
     * @param extensions the file extensions to bind
     */
    public static void register(@NotNull NTConfig config, @NotNull TomlFormat format, @NotNull String... extensions) {
        config.registerFormat(format, extensions);
    }
}
