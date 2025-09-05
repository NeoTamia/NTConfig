package re.neotamia.config;

import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.registry.SerializerRegistry;
import re.neotamia.config.registry.TypeAdapterRegistry;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;

public class NTConfig {
    private final TypeAdapterRegistry typeAdapterRegistry = new TypeAdapterRegistry();
    private final SerializerRegistry serializerRegistry = new SerializerRegistry();

    public <T> void save(Path path, T config) {
        var serializer = serializerRegistry.getByExtension(path);
        if (serializer == null) throw new IllegalArgumentException("No serializer found for " + path);

        StringBuilder sb = new StringBuilder();
        for (Field field : config.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(config);
                if (value != null) {
                    sb.append(serializer.serialize(value));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(sb);
    }

    public <T> T load(Path path, Class<T> clazz) {
        return null;
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
}
