package re.neotamia.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.migration.ConfigMigrationManager;
import re.neotamia.config.migration.MergeStrategy;
import re.neotamia.config.migration.MigrationHook;
import re.neotamia.config.migration.VersionUtils;
import re.neotamia.config.registry.FormatRegistry;
import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.file.FileConfig;
import re.neotamia.nightconfig.core.serde.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class NTConfig {
    private final FormatRegistry formatRegistry = new FormatRegistry();
    private final ObjectSerializer objectSerializer;
    private final ObjectDeserializer objectDeserializer;
    private ConfigMigrationManager migrationManager;

    /**
     * Constructs an NTConfig instance with standard object serializer and deserializer.
     */
    public NTConfig() {
        this(ObjectSerializer.standard(), ObjectDeserializer.standard());
    }

    /**
     * Constructs an NTConfig instance using the provided builders to create
     * the object serializer and deserializer.
     *
     * @param objectSerializerBuilder   the builder for the object serializer; must not be null
     * @param objectDeserializerBuilder the builder for the object deserializer; must not be null
     */
    public NTConfig(@NotNull ObjectSerializerBuilder objectSerializerBuilder, @NotNull ObjectDeserializerBuilder objectDeserializerBuilder) {
        this(objectSerializerBuilder.build(), objectDeserializerBuilder.build());
    }

    /**
     * Constructs an NTConfig instance with the specified object serializer and deserializer.
     *
     * @param objectSerializer   the object serializer to use; must not be null
     * @param objectDeserializer the object deserializer to use; must not be null
     */
    public NTConfig(@NotNull ObjectSerializer objectSerializer, @NotNull ObjectDeserializer objectDeserializer) {
        this.objectSerializer = objectSerializer;
        this.objectDeserializer = objectDeserializer;
    }

    /**
     * Serializes the provided configuration object and saves it to a file at the specified path.
     * This method creates a file configuration for the given path, serializes the fields of
     * the provided configuration object into it, and then saves the configuration to the file.
     *
     * @param <T>    the type of the configuration object
     * @param path   the path to the configuration file; must not be null
     * @param config the configuration object to serialize and save; must not be null
     * @return the file configuration used for saving; never null
     * @throws RuntimeException if any errors occur during the serialization or saving process
     */
    public <T> @NotNull FileConfig save(@NotNull Path path, @NotNull T config) throws RuntimeException {
        FileConfig fileConfig = FileConfig.builder(path).sync().build();
        return save(fileConfig, config);
    }

    /**
     * Serializes the provided configuration object and saves it to the specified file configuration.
     * This method serializes the fields of the given configuration object into the provided
     * file configuration and then saves the configuration to the file.
     *
     * @param <T>        the type of the configuration object
     * @param fileConfig the file configuration to save to; must not be null
     * @param config     the configuration object to serialize and save; must not be null
     * @return the provided file configuration after saving; never null
     * @throws RuntimeException if any errors occur during the serialization or saving process
     */
    public <T> @NotNull FileConfig save(@NotNull FileConfig fileConfig, @NotNull T config) throws RuntimeException {
        this.objectSerializer.serializeFields(config, fileConfig);
        fileConfig.save();
        return fileConfig;
    }

    /**
     * Loads and deserializes a configuration file into the provided instance.
     * This method loads the configuration from the specified file path and
     * deserializes the loaded data into the fields of the given instance.
     *
     * @param <T>      the type of the configuration object
     * @param path     the path to the configuration file; must not be null
     * @param instance the instance to populate with the deserialized configuration data; must not be null
     * @return the provided instance populated with the deserialized configuration data; never null
     * @throws RuntimeException if any errors occur during the deserialization process
     */
    public <T> @Nullable T load(@NotNull Path path, @NotNull T instance) throws RuntimeException {
        FileConfig fileConfig = FileConfig.builder(path).sync().build();
        fileConfig.load();
        this.objectDeserializer.deserializeFields(fileConfig, instance);
        return instance;
    }

    /**
     * Loads and deserializes a configuration file into an instance of the specified class type.
     * This method creates a new instance of the given class, loads the configuration from the
     * provided file path, and deserializes the loaded data into the instance fields.
     *
     * @param <T>   the type of the configuration object
     * @param path  the path to the configuration file; must not be null
     * @param clazz the class of the type that the configuration will be deserialized into; must not be null
     * @return an instance of the specified class populated with the deserialized configuration data; never null
     * @throws RuntimeException if the instance of the class cannot be created, or any errors occur during the deserialization process
     */
    public <T> @NotNull T load(@NotNull Path path, @NotNull Class<T> clazz) throws RuntimeException {
        FileConfig fileConfig = FileConfig.builder(path).sync().build();
        return load(fileConfig, clazz);
    }

    /**
     * Loads and deserializes a configuration file into an instance of the specified class type.
     * This method creates a new instance of the given class, loads the configuration from the
     * provided file configuration, and deserializes the loaded data into the instance fields.
     *
     * @param <T>        the type of the configuration object
     * @param fileConfig the file configuration to load; must not be null
     * @param clazz      the class of the type that the configuration will be deserialized into; must not be null
     * @return an instance of the specified class populated with the deserialized configuration data; never null
     * @throws RuntimeException if the instance of the class cannot be created, or any errors occur during the deserialization process
     */
    public <T> @NotNull T load(@NotNull FileConfig fileConfig, @NotNull Class<T> clazz) throws RuntimeException {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of class: " + clazz.getName(), e);
        }
        fileConfig.load();
        this.objectDeserializer.deserializeFields(fileConfig, instance);
        return instance;
    }

    /**
     * Registers a type adapter for serialization and deserialization.
     * This method allows the specifying of custom behavior for handling specific data types
     * during the serialization and deserialization process.
     *
     * @param <T>     the type of the object to serialize/deserialize
     * @param <R>     the type of the serialized form
     * @param adapter the type adapter to register; must not be null
     */
    public <T, R> void registerTypeAdapter(@NotNull TypeAdapter<T, R> adapter) {
        this.objectSerializer.registerSerializerForClass(adapter.valueClass(), adapter);
        this.objectDeserializer.registerDeserializerForClass(adapter.resultClass(), adapter.valueClass(), adapter);
    }

    /**
     * Registers a configuration format with its associated file extensions.
     *
     * @param form       The configuration format to register; must not be null.
     * @param extensions The file extensions associated with the format; at least one must be provided.
     * @throws IllegalArgumentException If no extensions are provided.
     */
    public void registerFormat(@NotNull ConfigFormat<?> form, @NotNull String... extensions) throws IllegalArgumentException {
        if (extensions.length == 0) throw new IllegalArgumentException("At least one extension must be provided");
        formatRegistry.register(form, extensions);
    }

    /**
     * Sets the naming strategy to be used by both the object serializer and object deserializer.
     *
     * @param strategy the naming strategy to apply; must not be null
     */
    public void setNamingStrategy(@NotNull NamingStrategy strategy) {
        this.objectSerializer.setNamingStrategy(strategy);
        this.objectDeserializer.setNamingStrategy(strategy);
    }

    /**
     * Loads a configuration with migration support.
     * If the loaded configuration version differs from the current template version,
     * migration will be performed according to the specified strategy.
     *
     * @param path            the configuration file path
     * @param clazz           the configuration class
     * @param currentTemplate the current configuration template with new defaults and version
     * @param strategy        the merge strategy to use (null for default)
     * @param <T>             the configuration type
     * @return the migration result containing the loaded/migrated configuration
     */
    public <T> ConfigMigrationManager.@NotNull MigrationResult<T> loadWithMigration(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate, MergeStrategy strategy) {
        ensureMigrationManager();

        // Load the existing configuration
        T loadedConfig;
        try {
            loadedConfig = load(path, clazz);
        } catch (Exception e) {
            if (!Files.exists(path)) {
                save(path, currentTemplate);
                return new ConfigMigrationManager.MigrationResult<>(currentTemplate, false, null, VersionUtils.extractVersion(currentTemplate), null);
            }
            throw e;
        }

        ConfigMigrationManager.MigrationResult<T> result = migrationManager.migrate(path, loadedConfig, currentTemplate, strategy);

        if (result.wasMigrated()) save(path, result.config());

        return result;
    }

    /**
     * Loads a configuration with migration support using the default merge strategy.
     */
    public <T> ConfigMigrationManager.MigrationResult<T> loadWithMigration(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate) {
        return loadWithMigration(path, clazz, currentTemplate, null);
    }

    /**
     * Loads and updates a configuration, always saving the result.
     * This is useful for ensuring configuration files are up to date with current templates.
     */
    public <T> ConfigMigrationManager.@NotNull MigrationResult<T> loadAndUpdate(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate, MergeStrategy strategy) {
        ConfigMigrationManager.MigrationResult<T> result = loadWithMigration(path, clazz, currentTemplate, strategy);
        // Always save to ensure a file is up to date (comments, formatting, etc.)
        save(path, result.config());
        return result;
    }

    /**
     * Loads and updates a configuration using the default merge strategy.
     */
    public <T> ConfigMigrationManager.MigrationResult<T> loadAndUpdate(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate) {
        return loadAndUpdate(path, clazz, currentTemplate, null);
    }

    /**
     * Gets the migration manager, creating it if necessary.
     */
    public ConfigMigrationManager getMigrationManager() {
        ensureMigrationManager();
        return migrationManager;
    }

    /**
     * Sets a custom migration manager.
     */
    public void setMigrationManager(ConfigMigrationManager migrationManager) {
        this.migrationManager = migrationManager;
    }

    /**
     * Adds a migration hook.
     */
    public void addMigrationHook(MigrationHook hook) {
        ensureMigrationManager();
        migrationManager.addHook(hook);
    }

    /**
     * Sets the default merge strategy for migrations.
     */
    public void setDefaultMergeStrategy(MergeStrategy strategy) {
        ensureMigrationManager();
        migrationManager.setDefaultMergeStrategy(strategy);
    }

    /**
     * Gets the default merge strategy for migrations.
     */
    public MergeStrategy getDefaultMergeStrategy() {
        ensureMigrationManager();
        return migrationManager.getDefaultMergeStrategy();
    }

    private void ensureMigrationManager() {
        if (migrationManager == null) migrationManager = new ConfigMigrationManager();
    }
}
