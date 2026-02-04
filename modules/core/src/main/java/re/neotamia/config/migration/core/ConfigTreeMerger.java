package re.neotamia.config.migration.core;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to merge raw NightConfig trees.
 */
public final class ConfigTreeMerger {
    /**
     * Creates a new config tree merger.
     */
    public ConfigTreeMerger() {}

    /**
     * Result of a merge operation.
     *
     * @param config the merged config
     * @param merged whether any changes were applied
     */
    public record MergeResult(@NotNull Config config, boolean merged) {
        /**
         * Returns whether the merge applied changes.
         *
         * @return true if any changes were applied
         */
        public boolean wasMerged() {
            return merged;
        }
    }

    /**
     * Merges missing keys from {@code defaults} into {@code target}.
     *
     * @param target   the config to mutate
     * @param defaults the defaults to apply
     * @return the merge result
     */
    public @NotNull MergeResult mergeMissingOnly(@NotNull Config target, @NotNull Config defaults) {
        boolean changed = mergeMissingRecursive(target, defaults);
        return new MergeResult(target, changed);
    }

    /**
     * Builds a new config by starting from {@code defaults} and overlaying {@code overrides}.
     *
     * @param defaults  the defaults to copy
     * @param overrides values that override defaults
     * @return a new merged config
     */
    public @NotNull Config mergeWithDefaults(@NotNull Config defaults, @NotNull Config overrides) {
        Config result = deepCopy(defaults);
        overlayRecursive(result, overrides);
        return result;
    }

    private boolean mergeMissingRecursive(@NotNull Config target, @NotNull Config defaults) {
        boolean changed = false;
        for (var entry : defaults.entrySet()) {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();
            Object currentValue = target.get(key);
            if (currentValue == null) {
                target.set(key, deepCopyValue(defaultValue));
                changed = true;
                continue;
            }
            if (currentValue instanceof Config currentConfig && defaultValue instanceof Config defaultConfig) {
                changed |= mergeMissingRecursive(currentConfig, defaultConfig);
            }
        }
        return changed;
    }

    private void overlayRecursive(@NotNull Config target, @NotNull Config overrides) {
        for (var entry : overrides.entrySet()) {
            String key = entry.getKey();
            Object overrideValue = entry.getValue();
            Object currentValue = target.get(key);
            if (currentValue instanceof Config currentConfig && overrideValue instanceof Config overrideConfig) {
                overlayRecursive(currentConfig, overrideConfig);
            } else {
                target.set(key, deepCopyValue(overrideValue));
            }
        }
    }

    private @NotNull Config deepCopy(@NotNull Config source) {
        Config copy = createEmptyLike(source);
        for (var entry : source.entrySet()) {
            copy.set(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private @NotNull Config createEmptyLike(@NotNull Config source) {
        return ConfigMigrationHelpers.createSubConfig(source);
    }

    private Object deepCopyValue(Object value) {
        if (value instanceof Config configValue) {
            return deepCopy(configValue);
        }
        if (value instanceof List<?> listValue) {
            List<Object> copy = new ArrayList<>(listValue.size());
            for (Object item : listValue) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof Map<?, ?> mapValue) {
            return new LinkedHashMap<>(mapValue);
        }
        return value;
    }
}
