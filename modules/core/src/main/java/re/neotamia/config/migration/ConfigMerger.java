package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Handles merging of configuration objects according to different strategies.
 */
public class ConfigMerger {

    /**
     * Merges an old configuration with a new configuration template according to the specified strategy.
     *
     * @param oldConfig the existing configuration (loaded from file)
     * @param newConfig the new configuration template (with current defaults)
     * @param strategy  the merge strategy to use
     * @param <T>       the configuration type
     * @return the merged configuration
     * @throws Exception if merging fails
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(@Nullable T oldConfig, @Nullable T newConfig, MergeStrategy strategy) throws Exception {
        if (oldConfig == null) return newConfig;
        if (newConfig == null) return oldConfig;

        Class<T> clazz = (Class<T>) oldConfig.getClass();

        return switch (strategy) {
            case OVERRIDE -> newConfig;
            case VERSION_ONLY -> mergeVersionOnly(oldConfig, newConfig);
            case MERGE_MISSING_ONLY -> mergeMissingOnly(oldConfig, newConfig, clazz);
            default -> throw new IllegalArgumentException("Unknown merge strategy: " + strategy);
        };
    }

    /**
     * Creates a deep copy of a configuration object.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T copy(@Nullable T config) throws Exception {
        if (config == null) return null;

        Class<T> clazz = (Class<T>) config.getClass();
        T copy = clazz.getDeclaredConstructor().newInstance();

        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (shouldExcludeField(field)) continue;

            field.setAccessible(true);
            Object value = field.get(config);

            if (value != null) {
                // For simple types, just copy the value
                if (isPrimitiveLike(value.getClass()) || value.getClass().isEnum()) {
                    field.set(copy, value);
                } else if (value instanceof List<?>) {
                    field.set(copy, new ArrayList<>((List<?>) value));
                } else if (value instanceof Map<?, ?>) {
                    field.set(copy, new LinkedHashMap<>((Map<?, ?>) value));
                } else {
                    // For complex objects, recursively copy (shallow copy for now)
                    field.set(copy, value);
                }
            }
        }

        return copy;
    }

    private <T> T mergeVersionOnly(T oldConfig, T newConfig) throws Exception {
        T result = copy(oldConfig);

        // Only update the version field
        ConfigVersion newVersion = VersionUtils.extractVersion(newConfig);
        if (newVersion != null) {
            VersionUtils.setVersion(result, newVersion);
        }

        return result;
    }

    private <T> T mergeMissingOnly(T oldConfig, T newConfig, Class<T> clazz) throws Exception {
        T result = copy(oldConfig);

        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (shouldExcludeField(field)) continue;

            field.setAccessible(true);
            Object oldValue = field.get(oldConfig);
            Object newValue = field.get(newConfig);

            // Only add missing fields (where old value is null but new value exists)
            if (oldValue == null && newValue != null) {
                field.set(result, newValue);
            }
        }

        // Always update version
        ConfigVersion newVersion = VersionUtils.extractVersion(newConfig);
        if (newVersion != null) {
            VersionUtils.setVersion(result, newVersion);
        }

        return result;
    }

    private boolean shouldExcludeField(@NotNull Field field) {
        return false;
    }

    private @NotNull List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    private boolean isPrimitiveLike(@NotNull Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class;
    }
}
