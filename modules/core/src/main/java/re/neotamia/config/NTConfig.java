package re.neotamia.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.annotation.ConfigHeader;
import re.neotamia.config.migration.ConfigMigrationManager;
import re.neotamia.config.migration.MergeStrategy;
import re.neotamia.config.migration.MigrationHook;
import re.neotamia.config.migration.VersionUtils;
import re.neotamia.config.registry.FormatRegistry;
import re.neotamia.config.saveable.Saveable;
import re.neotamia.config.saveable.SaveableCommented;
import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.file.CommentedFileConfig;
import re.neotamia.nightconfig.core.file.FileConfig;
import re.neotamia.nightconfig.core.serde.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NTConfig {
    private final FormatRegistry formatRegistry = new FormatRegistry();
    private final SerdeContext serdeContext;
    private ConfigMigrationManager migrationManager;

    /**
     * Constructs an NTConfig instance with standard object serializer and deserializer.
     */
    public NTConfig() {
        this(SerdeContext.builder().build());
    }

    /**
     * Constructs an NTConfig instance using the provided builders to create
     * the object serializer and deserializer.
     *
     * @param objectSerializerBuilder   the builder for the object serializer; must not be null
     * @param objectDeserializerBuilder the builder for the object deserializer; must not be null
     */
    public NTConfig(@NotNull ObjectSerializerBuilder objectSerializerBuilder, @NotNull ObjectDeserializerBuilder objectDeserializerBuilder) {
        this(new SerdeContext(objectSerializerBuilder.build(), objectDeserializerBuilder.build()));
    }

    /**
     * Constructs an NTConfig instance with the specified object serializer and deserializer.
     *
     * @param objectSerializer   the object serializer to use; must not be null
     * @param objectDeserializer the object deserializer to use; must not be null
     */
    public NTConfig(@NotNull ObjectSerializer objectSerializer, @NotNull ObjectDeserializer objectDeserializer) {
        this(new SerdeContext(objectSerializer, objectDeserializer));
    }

    /**
     * Constructs an NTConfig instance with the specified SerdeContext.
     *
     * @param serdeContext the serde context to use; must not be null
     */
    public NTConfig(@NotNull SerdeContext serdeContext) {
        this.serdeContext = serdeContext;
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
    public <T> @NotNull FileConfig save(@NotNull String path, @NotNull T config) throws RuntimeException {
        return save(Path.of(path), config);
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
        if (fileConfig instanceof CommentedFileConfig commentedFileConfig) {
            ConfigHeader header = config.getClass().getAnnotation(ConfigHeader.class);
            if (header != null && !header.value().isEmpty())
                commentedFileConfig.setHeaderComment(header.value());
        }

        saveToConfig(fileConfig, config);

        fileConfig.save();
        return fileConfig;
    }

    /**
     * Saves the provided configuration object to the specified file configuration. The saving behavior
     * depends on the type of the configuration object and the file configuration.
     *
     * @param fileConfig the file configuration to which the data will be saved; must not be null
     * @param config the configuration object to be saved; must not be null
     * @param <T> the type of the configuration object
     */
    private <T> void saveToConfig(@NotNull FileConfig fileConfig, @NotNull T config) throws NTConfigException {
        fileConfig.setSerdeContext(this.serdeContext);
        if (config instanceof SaveableCommented saveableCommented && fileConfig instanceof CommentedFileConfig commentedFileConfig)
            saveableCommented.save(commentedFileConfig);
        else if (config instanceof Saveable saveable)
            saveable.save(fileConfig);
        else {
            try {
                this.serdeContext.getSerializer().serializeFields(config, fileConfig);
            } catch (SerdeException e) {
                throw new NTConfigException(buildSerdeMessage("serialize", config.getClass().getName(), e), e);
            }
        }
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
    public <T> @Nullable T load(@NotNull String path, @NotNull T instance) throws RuntimeException {
        return load(Path.of(path), instance);
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
        loadFromConfig(fileConfig, instance);
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
        loadFromConfig(fileConfig, instance);
        return instance;
    }

    /**
     * Loads configuration data from the specified {@link FileConfig} into the given instance.
     * The method determines the appropriate loading mechanism based on the type of the instance provided.
     *
     * @param fileConfig the configuration file object from which data will be loaded; must not be null
     * @param instance the instance into which the configuration data will be loaded; must not be null
     * @param <T> the type of the instance object
     */
    private <T> void loadFromConfig(@NotNull FileConfig fileConfig, @NotNull T instance) throws NTConfigException {
        fileConfig.setSerdeContext(this.serdeContext);
        if (instance instanceof SaveableCommented saveableCommented && fileConfig instanceof CommentedFileConfig commentedFileConfig)
            saveableCommented.load(commentedFileConfig);
        else if (instance instanceof Saveable saveable)
            saveable.load(fileConfig);
        else {
            try {
                this.serdeContext.getDeserializer().deserializeFields(fileConfig, instance);
            } catch (SerdeException e) {
                throw new NTConfigException(buildSerdeMessage("deserialize", instance.getClass().getName(), e), e);
            }
        }
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
        this.serdeContext.registerTypeAdapter(adapter);
    }

    /**
     * Returns the SerdeContext used by this NTConfig.
     * <p>
     * The SerdeContext is automatically attached to configs during save/load
     * operations,
     * enabling typed operations like {@code config.setTyped()} and
     * {@code config.getTyped()}.
     *
     * @return the SerdeContext; never null
     */
    public @NotNull SerdeContext getSerdeContext() {
        return this.serdeContext;
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
        this.serdeContext.getSerializer().setNamingStrategy(strategy);
        this.serdeContext.getDeserializer().setNamingStrategy(strategy);
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
    public <T> ConfigMigrationManager.MigrationResult<T> loadWithMigration(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate,
                                                                           MergeStrategy strategy) {
        ensureMigrationManager();

        // Load the existing configuration
        T loadedConfig;
        try {
            loadedConfig = load(path, clazz);
        } catch (Exception e) {
            if (!Files.exists(path)) {
                try (var fileConfig = save(path, currentTemplate)) {
                    // Return result indicating no migration was needed (new file created)
                    return new ConfigMigrationManager.MigrationResult<>(currentTemplate, false, null,
                            VersionUtils.extractVersion(currentTemplate), null);
                }
            }
            throw e;
        }

        ConfigMigrationManager.MigrationResult<T> result = migrationManager.migrate(path, loadedConfig, currentTemplate, strategy);

        if (result.wasMigrated()) {
            var file = save(path, result.config());
            file.close();
        }

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
    public <T> ConfigMigrationManager.MigrationResult<T> loadAndUpdate(@NotNull Path path, @NotNull Class<T> clazz, @NotNull T currentTemplate, MergeStrategy strategy) {
        ConfigMigrationManager.MigrationResult<T> result = loadWithMigration(path, clazz, currentTemplate, strategy);
        // Always save to ensure a file is up to date (comments, formatting, etc.)
        var fileConfig = save(path, result.config());
        fileConfig.close();
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

    /**
     * Constructs a detailed error message for serialization/deserialization issues, providing information
     * about the root class, the field path, and the specific cause of the failure.
     *
     * @param action          A description of the action being performed, such as "serialize" or "deserialize".
     * @param rootClassName   The fully qualified name of the root configuration class involved in the process.
     * @param exception       The {@link SerdeException} containing details about the serialization/deserialization error.
     * @return A non-null string describing the failure, including the action, root class, field path, and cause.
     */
    private static @NotNull String buildSerdeMessage(@NotNull String action, @NotNull String rootClassName, @NotNull SerdeException exception) {
        List<String> fieldPath = new ArrayList<>();
        List<String> classPath = new ArrayList<>();
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (!(current instanceof SerdeException) || current.getMessage() == null) {
                continue;
            }
            Matcher matcher = FIELD_PATTERN.matcher(current.getMessage());
            if (matcher.find()) {
                String fieldDescriptor = matcher.group(1);
                FieldInfo fieldInfo = parseFieldDescriptor(fieldDescriptor);
                fieldPath.add(fieldInfo.fieldName());
                classPath.add(fieldInfo.classPath());
            }
        }
        String path = fieldPath.isEmpty() ? "<unknown>" : String.join(".", fieldPath);
        String classPathDisplay = classPath.isEmpty() ? "<unknown>" : String.join(" -> ", classPath);
        String rootCause = findRootCauseMessage(exception);
        String simpleRoot = rootClassName.substring(rootClassName.lastIndexOf('.') + 1);
        return "Failed to " + action + " configuration class " + simpleRoot + " (" + rootClassName + ")"
                + ". Field path: " + path + " (" + classPathDisplay + "). Cause: " + rootCause;
    }

    /**
     * Traverses the causal chain of the given throwable to determine the root cause
     * and returns a descriptive message for it. If the root cause has no message
     * or its message is blank, the class name of the root cause is returned instead.
     *
     * @param throwable the throwable to analyze for its root cause; must not be null
     * @return the message of the root cause, or the class name if the message is null or blank
     */
    private static @NotNull String findRootCauseMessage(@NotNull Throwable throwable) {
        Throwable current = throwable;
        Throwable last = throwable;
        while (current != null) {
            last = current;
            current = current.getCause();
        }
        String message = last.getMessage();
        return message == null || message.isBlank() ? last.getClass().getName() : message;
    }

    /**
     * Parses the provided field descriptor to extract detailed information about the field.
     *
     * @param fieldDescriptor A non-null string representing the field descriptor. It typically consists of a class name,
     *                        followed by a field name, separated by a dot. It may also include additional whitespace
     *                        and descriptive parts that are trimmed and processed.
     * @return A {@link FieldInfo} object containing the parsed field information, including a formatted field identifier
     *         and the raw field name. If the input descriptor lacks a clear dot-based separation of class and field,
     *         it returns the entire descriptor as both the identifier and field name.
     */
    private static @NotNull FieldInfo parseFieldDescriptor(@NotNull String fieldDescriptor) {
        String[] parts = fieldDescriptor.trim().split("\\s+");
        String lastToken = parts.length == 0 ? fieldDescriptor : parts[parts.length - 1];
        int lastDot = lastToken.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= lastToken.length() - 1) {
            return new FieldInfo(lastToken, lastToken);
        }
        String className = lastToken.substring(0, lastDot);
        String fieldName = lastToken.substring(lastDot + 1);
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return new FieldInfo(simpleClassName + "." + fieldName, fieldName);
    }

    private static final Pattern FIELD_PATTERN = Pattern.compile("field `([^`]+)`");

    private record FieldInfo(@NotNull String classPath, @NotNull String fieldName) {}
}
