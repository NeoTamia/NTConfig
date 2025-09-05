package re.neotamia.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import re.neotamia.config.Serializer;
import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.registry.TypeAdapterRegistry;

import java.util.Set;

public class JsonSerializer implements Serializer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final TypeAdapterRegistry registry;

    public JsonSerializer(TypeAdapterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> String serialize(T obj) {
        TypeAdapter<T> adapter = registry.get((Class<T>) obj.getClass());
        if (adapter != null) {
            Object tree = adapter.serialize(obj);
            return gson.toJson(tree);
        }
        return gson.toJson(obj);
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        TypeAdapter<T> adapter = registry.get(type);
        if (adapter != null) {
            Object tree = gson.fromJson(data, Object.class);
            return adapter.deserialize(tree, type);
        }
        return gson.fromJson(data, type);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("json");
    }
}
