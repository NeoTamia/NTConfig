package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.annotation.ConfigVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for working with configuration versions.
 */
public class VersionUtils {

    /**
     * Extracts the version from a configuration object.
     *
     * @param config the configuration object
     * @return the extracted version, or null if no version field is found
     * @throws RuntimeException if version extraction fails
     */
    public static @Nullable re.neotamia.config.migration.ConfigVersion extractVersion(@Nullable Object config) {
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
                return new re.neotamia.config.migration.ConfigVersion(defaultVersion);
            }

            return convertToConfigVersion(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract version from config", e);
        }
    }

    /**
     * Sets the version in a configuration object.
     *
     * @param config  the configuration object
     * @param version the version to set
     * @throws RuntimeException if version setting fails
     */
    public static void setVersion(@Nullable Object config, re.neotamia.config.migration.@Nullable ConfigVersion version) {
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
     * Finds the field annotated with @ConfigVersion in the given class.
     *
     * @param clazz the class to search
     * @return the version field, or null if not found
     */
    public static @Nullable Field findVersionField(@NotNull Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.isAnnotationPresent(ConfigVersion.class))
                return field;
        }
        return null;
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
    public static re.neotamia.config.migration.@Nullable ConfigVersion getDefaultVersion(@NotNull Class<?> clazz) {
        Field versionField = findVersionField(clazz);
        if (versionField == null) return null;

        ConfigVersion annotation = versionField.getAnnotation(ConfigVersion.class);
        return new re.neotamia.config.migration.ConfigVersion(annotation.defaultVersion());
    }

    private static re.neotamia.config.migration.@NotNull ConfigVersion convertToConfigVersion(@NotNull Object value) {
        return switch (value) {
            case String s -> new re.neotamia.config.migration.ConfigVersion(s);
            case Integer i -> new re.neotamia.config.migration.ConfigVersion(i);
            case Number number -> new re.neotamia.config.migration.ConfigVersion(number.intValue());
            default -> throw new IllegalArgumentException("Version field must be String, int, or Integer, but was: " + value.getClass().getName());
        };
    }

    private static Object convertFromConfigVersion(re.neotamia.config.migration.@NotNull ConfigVersion version, @NotNull Class<?> fieldType) {
        if (fieldType == String.class)
            return version.getVersion();
        if (fieldType == int.class || fieldType == Integer.class)
            return version.getMajor();
        throw new IllegalArgumentException("Version field must be String, int, or Integer, but was: " + fieldType.getName());
    }
}