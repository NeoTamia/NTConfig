package re.neotamia.config.json;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.nightconfig.json.JsonFormat;

/**
 * Helper methods to register the JSON format for {@link NTConfig}.
 */
public final class JsonModule {
    /**
     * Default extensions for JSON configs.
     */
    public static final String[] DEFAULT_EXTENSIONS = {"json"};

    /**
     * Utility class.
     */
    private JsonModule() {}

    /**
     * Registers the default JSON format (fancy) with the default extensions.
     *
     * @param config the NTConfig instance to register on
     */
    public static void register(@NotNull NTConfig config) {
        register(config, JsonFormat.fancyInstance(), DEFAULT_EXTENSIONS.clone());
    }

    /**
     * Registers a custom JSON format and extensions.
     *
     * @param config     the NTConfig instance to register on
     * @param format     the JSON format to use
     * @param extensions the file extensions to bind
     */
    public static void register(@NotNull NTConfig config, @NotNull JsonFormat<?> format, @NotNull String... extensions) {
        config.registerFormat(format, extensions);
    }
}
