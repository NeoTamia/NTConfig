package re.neotamia.config;

import re.neotamia.config.adapter.AdapterContext;
import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.annotation.Comment;
import re.neotamia.config.annotation.ConfigProperty;
import re.neotamia.config.annotation.Exclude;
import re.neotamia.config.annotation.SerializedName;
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

    public <T> void save(Path path, T config) {
        var serializer = serializerRegistry.getByExtension(path);
        if (serializer == null) throw new IllegalArgumentException("No serializer found for " + path);
        try {
            String out;
            if (serializer.supportsComments()) {
                CommentedTree commentedTree = toCommentedTree(config, AdapterContext.root());
                out = serializer.fromCommentedTree(commentedTree);
            } else {
                Object tree = toTree(config, AdapterContext.root());
                out = serializer.fromTree(tree);
            }
            Files.writeString(path, out);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save config to " + path, e);
        }
    }

    public <T> T load(Path path, Class<T> clazz) {
        var serializer = serializerRegistry.getByExtension(path);
        if (serializer == null) throw new IllegalArgumentException("No serializer found for " + path);
        try {
            String data = Files.readString(path);
            Object root = serializer.toTree(data);
            AdapterContext ctx = AdapterContext.root();
            TypeAdapter<T> adapter = typeAdapterRegistry.get(clazz);
            if (adapter != null)
                return adapter.deserialize(root, clazz, ctx);
            return fromTree(root, clazz, null, ctx);
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

    public TypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }

    public SerializerRegistry getSerializerRegistry() {
        return serializerRegistry;
    }

    private boolean isPrimitiveLike(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class;
    }

    @SuppressWarnings("unchecked")
    private <T> T castScalar(Object node, Class<T> type) {
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

    private String resolveFieldName(Field f) {
        if (f.isAnnotationPresent(SerializedName.class))
            return f.getAnnotation(SerializedName.class).value();
        if (f.isAnnotationPresent(ConfigProperty.class)) {
            String n = f.getAnnotation(ConfigProperty.class).name();
            if (n != null && !n.isEmpty()) return n;
        }
        return f.getName();
    }

    @SuppressWarnings("unchecked")
    private <T> T fromTree(Object node, Class<T> type, Field field, AdapterContext ctx) throws Exception {
        if (node == null) return null;
        TypeAdapter<T> adapter = typeAdapterRegistry.get(type);
        if (adapter != null)
            return adapter.deserialize(node, type, ctx);
        if (isPrimitiveLike(type) || type.isEnum()) return castScalar(node, type);

        Object fieldName = field != null ? field.getName() : ctx.path();
        if (List.class.isAssignableFrom(type)) {
            if (!(node instanceof List<?> inList)) throw new IllegalArgumentException("Expected list for field " + fieldName);

            // Determine element type
            Class<?> elemType = Object.class;
            if (field != null) {
                Type g = field.getGenericType();
                if (g instanceof ParameterizedType pt) {
                    Type arg = pt.getActualTypeArguments()[0];
                    if (arg instanceof Class<?> c) elemType = c;
                }
            }
            List<Object> out = new ArrayList<>(inList.size());
            int idx = 0;
            for (Object item : inList) {
                AdapterContext childCtx = ctx.child(String.valueOf(idx), null);
                Object mapped = (elemType == Object.class) ? item : fromTree(item, (Class<Object>) elemType, null, childCtx);
                out.add(mapped);
                idx++;
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
                AdapterContext childCtx = ctx.child(key, null);
                Object mapped = (valType == Object.class) ? e.getValue() : fromTree(e.getValue(), (Class<Object>) valType, null, childCtx);
                out.put(key, mapped);
            }
            return (T) out;
        }
        // POJO
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
            AdapterContext childCtx = ctx.child(key, f);
            Object value = fromTree(child, (Class<Object>) f.getType(), f, childCtx);
            f.set(instance, value);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Object toTree(Object obj, AdapterContext ctx) throws Exception {
        if (obj == null) return null;
        Class<?> type = obj.getClass();
        TypeAdapter<Object> adapter = typeAdapterRegistry.get((Class<Object>) type);
        if (adapter != null) return adapter.serialize(obj, ctx);

        if (isPrimitiveLike(type)) return obj;
        if (type.isEnum()) return ((Enum<?>) obj).name();
        if (obj instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            int idx = 0;
            for (Object item : list) {
                out.add(toTree(item, ctx.child(String.valueOf(idx++), null)));
            }
            return out;
        }
        if (obj instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                out.put(key, toTree(e.getValue(), ctx.child(key, null)));
            }
            return out;
        }
        // POJO
        Map<String, Object> out = new LinkedHashMap<>();
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(Exclude.class)) continue;
            if (f.isAnnotationPresent(ConfigProperty.class) && f.getAnnotation(ConfigProperty.class).exclude()) continue;
            f.setAccessible(true);
            String key = resolveFieldName(f);
            Object value = f.get(obj);
            if (value == null) continue;
            out.put(key, toTree(value, ctx.child(key, f)));
        }
        return out;
    }

    private CommentedTree toCommentedTree(Object obj, AdapterContext ctx) throws Exception {
        Object tree = toTree(obj, ctx);
        CommentedTree commentedTree = new CommentedTree(tree);
        
        if (obj != null && !isPrimitiveLike(obj.getClass()) && !obj.getClass().isEnum() && 
            !(obj instanceof List<?>) && !(obj instanceof Map<?, ?>)) {
            // POJO - collect field comments
            collectFieldComments(obj.getClass(), commentedTree);
        }
        
        return commentedTree;
    }

    private void collectFieldComments(Class<?> type, CommentedTree commentedTree) {
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(Exclude.class)) continue;
            if (f.isAnnotationPresent(ConfigProperty.class) && f.getAnnotation(ConfigProperty.class).exclude()) continue;
            
            String fieldName = resolveFieldName(f);
            String comment = null;
            
            // Check @Comment annotation
            if (f.isAnnotationPresent(Comment.class)) {
                comment = f.getAnnotation(Comment.class).value();
            }
            // Check @ConfigProperty description (value())
            else if (f.isAnnotationPresent(ConfigProperty.class)) {
                String desc = f.getAnnotation(ConfigProperty.class).value();
                if (desc != null && !desc.trim().isEmpty()) {
                    comment = desc;
                }
            }
            
            if (comment != null) {
                commentedTree.addFieldComment(fieldName, comment);
            }
        }
    }
}
