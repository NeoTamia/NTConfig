package re.neotamia.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.annotation.*;
import re.neotamia.config.migration.ConfigMigrationManager;
import re.neotamia.config.migration.MergeStrategy;
import re.neotamia.config.migration.MigrationHook;
import re.neotamia.config.migration.VersionUtils;
import re.neotamia.config.naming.NamingStrategy;
import re.neotamia.config.registry.SerializerRegistry;
import re.neotamia.config.registry.TypeAdapterRegistry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NTConfig {
    private final TypeAdapterRegistry typeAdapterRegistry = new TypeAdapterRegistry();
    private final SerializerRegistry serializerRegistry = new SerializerRegistry();
    private @NotNull NamingStrategy namingStrategy = NamingStrategy.IDENTITY;
    private ConfigMigrationManager migrationManager;

    public <T> void save(@NotNull Path path, T config) {
        var serializer = serializerRegistry.getByExtension(path);
        if (serializer == null) throw new IllegalArgumentException("No serializer found for " + path);
        try {
            String out;
            if (serializer.supportsComments()) {
                CommentedTree commentedTree = toCommentedTree(config);
                out = serializer.fromCommentedTree(commentedTree);
            } else {
                Object tree = toTree(config);
                out = serializer.fromTree(tree);
            }
            Files.writeString(path, out);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save config to " + path, e);
        }
    }

    public <T> @Nullable T load(@NotNull Path path, @NotNull Class<T> clazz) {
        var serializer = serializerRegistry.getByExtension(path);
        if (serializer == null) throw new IllegalArgumentException("No serializer found for " + path);
        try {
            String data = Files.readString(path);
            Object root = serializer.toTree(data);
            TypeAdapter<T> adapter = typeAdapterRegistry.get(clazz);
            if (adapter != null)
                return adapter.deserialize(root, clazz);
            return fromTree(root, clazz, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config from " + path, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse config from " + path, e);
        }
    }

    public <T> void registerTypeAdapter(Class<T> clazz, TypeAdapter<T> adapter) {
        typeAdapterRegistry.register(clazz, adapter);
    }

    public void registerSerializer(Serializer serializer) {
        serializerRegistry.register(serializer);
    }

    public @NotNull TypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }

    public @NotNull SerializerRegistry getSerializerRegistry() {
        return serializerRegistry;
    }

    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    public void setNamingStrategy(@Nullable NamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy != null ? namingStrategy : NamingStrategy.IDENTITY;
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

        if (result.wasMigrated())
            save(path, result.config());

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
        if (migrationManager == null)
            migrationManager = new ConfigMigrationManager();
    }

    private boolean isPrimitiveLike(@NotNull Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T castScalar(@Nullable Object node, @NotNull Class<T> type) {
        if (node == null) return null;
        if (type.isInstance(node)) return (T) node;
        if (type == String.class) return (T) String.valueOf(node);
        if (node instanceof Number n) {
            if (type == Integer.class || type == int.class) return (T) Integer.valueOf(n.intValue());
            if (type == Long.class || type == long.class) return (T) Long.valueOf(n.longValue());
            if (type == Double.class || type == double.class) return (T) Double.valueOf(n.doubleValue());
            if (type == Float.class || type == float.class) return (T) Float.valueOf(n.floatValue());
            if (type == Short.class || type == short.class) return (T) Short.valueOf(n.shortValue());
            if (type == Byte.class || type == byte.class) return (T) Byte.valueOf(n.byteValue());
        }
        if (node instanceof Boolean b && (type == Boolean.class || type == boolean.class)) return (T) b;
        if (type.isEnum() && node instanceof String s) {
            return (T) Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), s);
        }
        throw new IllegalArgumentException("Cannot cast scalar node " + node + " to " + type.getName());
    }

    private String resolveFieldName(@NotNull Field f) {
        var name = f.getName();
        if (f.isAnnotationPresent(SerializedName.class))
            name = f.getAnnotation(SerializedName.class).value();
        if (f.isAnnotationPresent(ConfigProperty.class)) {
            String n = f.getAnnotation(ConfigProperty.class).name();
            if (n != null && !n.isEmpty())
                name = n;
        }
        return namingStrategy.transform(name);
    }

    @SuppressWarnings("unchecked")
    private <T> T fromTree(@Nullable Object node, @NotNull Class<T> type, @Nullable Field field) throws Exception {
        if (node == null) return null;
        TypeAdapter<T> adapter = typeAdapterRegistry.get(type);
        if (adapter != null)
            return adapter.deserialize(node, type);
        if (isPrimitiveLike(type) || type.isEnum()) return castScalar(node, type);

        Object fieldName = field != null ? field.getName() : "unknown";
        if (List.class.isAssignableFrom(type)) {
            if (!(node instanceof List<?> inList)) throw new IllegalArgumentException("Expected list for field " + fieldName);

            Class<?> elemType = Object.class;
            if (field != null) {
                Type g = field.getGenericType();
                if (g instanceof ParameterizedType pt) {
                    Type arg = pt.getActualTypeArguments()[0];
                    if (arg instanceof Class<?> c) elemType = c;
                }
            }
            List<Object> out = new ArrayList<>(inList.size());
            for (Object item : inList) {
                Object mapped = (elemType == Object.class) ? item : fromTree(item, (Class<Object>) elemType, null);
                out.add(mapped);
            }
            return (T) out;
        }
        if (Map.class.isAssignableFrom(type)) {
            if (!(node instanceof Map<?, ?> inMap)) throw new IllegalArgumentException("Expected map for field " + fieldName);

            Class<?> keyType = String.class;
            Class<?> valType = Object.class;
            if (field != null) {
                Type g = field.getGenericType();
                if (g instanceof ParameterizedType pt) {
                    Type k = pt.getActualTypeArguments()[0];
                    Type v = pt.getActualTypeArguments()[1];
                    if (k instanceof Class<?> kc) keyType = kc;
                    if (v instanceof Class<?> vc) valType = vc;
                }
            }
            if (keyType != String.class) throw new IllegalArgumentException("Only String keys are supported in maps for now");

            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : inMap.entrySet()) {
                String key = String.valueOf(e.getKey());
                Object mapped = (valType == Object.class) ? e.getValue() : fromTree(e.getValue(), (Class<Object>) valType, null);
                out.put(key, mapped);
            }
            return (T) out;
        }
        if (!(node instanceof Map<?, ?> map))
            throw new IllegalArgumentException("Expected object for type " + type.getName());

        T instance = type.getDeclaredConstructor().newInstance();
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(Exclude.class)) continue;
            if (f.isAnnotationPresent(ConfigProperty.class) && f.getAnnotation(ConfigProperty.class).exclude()) continue;
            f.setAccessible(true);
            String key = resolveFieldName(f);
            Object child = map.get(key);
            if (child == null) continue;
            Object value = fromTree(child, (Class<Object>) f.getType(), f);
            f.set(instance, value);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Object toTree(@Nullable Object obj) throws Exception {
        if (obj == null) return null;
        Class<?> type = obj.getClass();
        TypeAdapter<Object> adapter = typeAdapterRegistry.get((Class<Object>) type);
        if (adapter != null) return adapter.serialize(obj);

        if (isPrimitiveLike(type)) return obj;
        if (type.isEnum()) return ((Enum<?>) obj).name();
        if (obj instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list)
                out.add(toTree(item));
            return out;
        }
        if (obj instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                out.put(key, toTree(e.getValue()));
            }
            return out;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(Exclude.class)) continue;
            if (f.isAnnotationPresent(ConfigProperty.class) && f.getAnnotation(ConfigProperty.class).exclude()) continue;
            f.setAccessible(true);
            String key = resolveFieldName(f);
            Object value = f.get(obj);
            if (value == null) continue;
            out.put(key, toTree(value));
        }
        return out;
    }

    private @NotNull CommentedTree toCommentedTree(@Nullable Object obj) throws Exception {
        Object tree = toTree(obj);
        CommentedTree commentedTree = new CommentedTree(tree);

        if (obj != null && !isPrimitiveLike(obj.getClass()) && !obj.getClass().isEnum() && !(obj instanceof List<?>) && !(obj instanceof Map<?, ?>)) {
            Class<?> type = obj.getClass();
            if (type.isAnnotationPresent(ConfigHeader.class)) {
                String headerText = type.getAnnotation(ConfigHeader.class).value();
                if (headerText != null && !headerText.trim().isEmpty()) {
                    commentedTree.setHeaderComment(headerText);
                }
            }

            collectFieldComments(type, commentedTree);
        }

        return commentedTree;
    }

    private void collectFieldComments(@NotNull Class<?> type, @NotNull CommentedTree commentedTree) {
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(Exclude.class)) continue;
            if (f.isAnnotationPresent(ConfigProperty.class) && f.getAnnotation(ConfigProperty.class).exclude()) continue;

            String fieldName = resolveFieldName(f);
            String comment = getCommentFromAnnotation(f);

            if (comment != null)
                commentedTree.addFieldComment(fieldName, comment);
        }
    }

    private static @Nullable String getCommentFromAnnotation(@NotNull Field f) {
        String comment = null;

        if (f.isAnnotationPresent(Comment.class))
            comment = f.getAnnotation(Comment.class).value();
        else if (f.isAnnotationPresent(ConfigProperty.class)) {
            String desc = f.getAnnotation(ConfigProperty.class).value();
            if (desc != null && !desc.trim().isEmpty())
                comment = desc;
        }
        return comment;
    }
}
