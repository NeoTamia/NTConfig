package re.neotamia.config.migration.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;

/**
 * Helper methods for common raw-config migration operations.
 */
public final class ConfigMigrationHelpers {
    /**
     * Utility class for migration helpers.
     */
    private ConfigMigrationHelpers() {
    }

    /**
     * Renames a configuration path (moves value and removes the old key).
     *
     * @param config   the config to mutate
     * @param fromPath source path
     * @param toPath   target path
     * @return true if the value was moved, false otherwise
     */
    public static boolean rename(@NotNull Config config, @NotNull String fromPath, @NotNull String toPath) {
        return move(config, fromPath, toPath, false);
    }

    /**
     * Moves a configuration value from one path to another.
     *
     * @param config    the config to mutate
     * @param fromPath  source path
     * @param toPath    target path
     * @param overwrite whether to overwrite an existing value at the target
     * @return true if the value was moved, false otherwise
     */
    public static boolean move(@NotNull Config config, @NotNull String fromPath, @NotNull String toPath, boolean overwrite) {
        Object value = config.get(fromPath);
        if (value == null) return false;
        if (!overwrite && config.get(toPath) != null) return false;
        config.set(toPath, value);
        config.remove(fromPath);
        return true;
    }

    /**
     * Copies a configuration value from one path to another.
     *
     * @param config    the config to mutate
     * @param fromPath  source path
     * @param toPath    target path
     * @param overwrite whether to overwrite an existing value at the target
     * @return true if the value was copied, false otherwise
     */
    public static boolean copy(@NotNull Config config, @NotNull String fromPath, @NotNull String toPath, boolean overwrite) {
        Object value = config.get(fromPath);
        if (value == null) return false;
        if (!overwrite && config.get(toPath) != null) return false;
        config.set(toPath, value);
        return true;
    }

    /**
     * Wraps a scalar value into an object under the provided nested key.
     * <p>
     * Example: {@code server = "monserver"} -> {@code server = { id = "monserver" }}.
     *
     * @param config   the config to mutate
     * @param path     the path to wrap
     * @param nestedKey the nested key to use for the old value
     * @return true if wrapping was performed, false otherwise
     */
    public static boolean wrapValue(@NotNull Config config, @NotNull String path, @NotNull String nestedKey) {
        Object value = config.get(path);
        if (value == null) return false;
        if (value instanceof Config) return false;
        Config wrapper = createSubConfig(config);
        wrapper.set(nestedKey, value);
        config.set(path, wrapper);
        return true;
    }

    /**
     * Creates an empty sub-config compatible with the provided config type.
     *
     * @param parent the parent config
     * @return a new empty config
     */
    public static @NotNull Config createSubConfig(@NotNull Config parent) {
        if (parent instanceof CommentedConfig) {
            return CommentedConfig.inMemory();
        }
        return Config.inMemory();
    }

    /**
     * Reads a configuration value at the given path.
     *
     * @param config the config to read
     * @param path   the path
     * @return the value or null if missing
     */
    public static @Nullable Object read(@NotNull Config config, @NotNull String path) {
        return config.get(path);
    }
}
