package re.neotamia.config.migration.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.annotation.ConfigVersion;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.NamingStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for working with configuration versions.
 */
public class VersionUtils {
    private static final ConcurrentHashMap<Class<?>, Optional<Field>> VERSION_FIELDS = new ConcurrentHashMap<>();
    /**
     * Utility class for version helpers.
     */
    private VersionUtils() {}

    /**
     * Extracts the version from a configuration object.
     *
     * @param config the configuration object
     * @return the extracted version, or null if no version field is found
     * @throws RuntimeException if version extraction fails
     */
    public static @Nullable MigrationVersion extractVersion(@Nullable Object config) {
        if (config == null) return null;

        Class<?> clazz = config.getClass();
        Field versionField = findVersionField(clazz);

        if (versionField == null) return null;

        try {
            versionField.setAccessible(true);
            Object value = versionField.get(config);

            if (value == null) {
                // Use default version from annotation
                ConfigVersion annotation = versionField.getAnnotation(ConfigVersion.class);
                String defaultVersion = annotation.defaultVersion();
                return new MigrationVersion(defaultVersion);
            }

            return convertToConfigVersion(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract version from config", e);
        }
    }

    /**
     * Extracts the version from a raw NightConfig tree using the config class as metadata.
     *
     * @param config         the raw config
     * @param clazz          the configuration class
     * @param namingStrategy the naming strategy used for serialization (nullable)
     * @return the extracted version, or null if not found
     */
    public static @Nullable MigrationVersion extractVersion(@NotNull Config config, @NotNull Class<?> clazz, @Nullable NamingStrategy namingStrategy) {
        Field versionField = findVersionField(clazz);
        if (versionField == null) return null;
        String key = versionField.getName();
        Object value = config.get(key);
        if (value == null) return null;
        return convertToConfigVersion(value);
    }

    /**
     * Sets the version in a configuration object.
     *
     * @param config  the configuration object
     * @param version the version to set
     * @throws RuntimeException if version setting fails
     */
    public static void setVersion(@Nullable Object config, @Nullable MigrationVersion version) {
        if (config == null || version == null) return;

        Class<?> clazz = config.getClass();
        Field versionField = findVersionField(clazz);

        if (versionField == null) return;

        try {
            versionField.setAccessible(true);
            Object value = convertFromConfigVersion(version, versionField.getType());
            versionField.set(config, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set version in config", e);
        }
    }

    /**
     * Sets the version in a raw NightConfig tree using the config class as metadata.
     *
     * @param config         the raw config
     * @param clazz          the configuration class
     * @param namingStrategy the naming strategy used for serialization (nullable)
     * @param version        the version to set
     */
    public static void setVersion(@NotNull Config config, @NotNull Class<?> clazz, @Nullable NamingStrategy namingStrategy,
                                  @Nullable MigrationVersion version) {
        if (version == null) return;
        Field versionField = findVersionField(clazz);
        if (versionField == null) return;
        String key = versionField.getName();
        Object converted = convertFromConfigVersion(version, versionField.getType());
        config.set(key, converted);
    }

    /**
     * Finds the field annotated with @ConfigVersion in the given class.
     *
     * @param clazz the class to search
     * @return the version field, or null if not found
     */
    public static @Nullable Field findVersionField(@NotNull Class<?> clazz) {
        Optional<Field> cached = VERSION_FIELDS.computeIfAbsent(clazz, key -> {
            for (Field field : key.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isAnnotationPresent(ConfigVersion.class)) {
                    return Optional.of(field);
                }
            }
            return Optional.empty();
        });
        return cached.orElse(null);
    }

    /**
     * Checks if a configuration class has a version field.
     *
     * @param clazz the class to check
     * @return true if the class has a version field
     */
    public static boolean hasVersionField(@NotNull Class<?> clazz) {
        return findVersionField(clazz) != null;
    }

    /**
     * Gets the default version from a configuration class.
     *
     * @param clazz the configuration class
     * @return the default version, or null if no version field exists
     */
    public static @Nullable MigrationVersion getDefaultVersion(@NotNull Class<?> clazz) {
        Field versionField = findVersionField(clazz);
        if (versionField == null) return null;

        ConfigVersion annotation = versionField.getAnnotation(ConfigVersion.class);
        return new MigrationVersion(annotation.defaultVersion());
    }

    private static @NotNull MigrationVersion convertToConfigVersion(@NotNull Object value) {
        return switch (value) {
            case String s -> new MigrationVersion(s);
            case Integer i -> new MigrationVersion(i);
            case Number number -> new MigrationVersion(number.intValue());
            default -> throw new IllegalArgumentException("Version field must be String, int, or Integer, but was: " + value.getClass().getName());
        };
    }

    private static Object convertFromConfigVersion(@NotNull MigrationVersion version, @NotNull Class<?> fieldType) {
        if (fieldType == String.class)
            return version.getVersion();
        if (fieldType == int.class || fieldType == Integer.class)
            return version.getMajor();
        throw new IllegalArgumentException("Version field must be String, int, or Integer, but was: " + fieldType.getName());
    }
}
