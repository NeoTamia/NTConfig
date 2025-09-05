package re.neotamia.config.registry;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.adapter.TypeAdapter;

import java.util.HashMap;
import java.util.Map;

public class TypeAdapterRegistry {
    private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();

    public <T> void register(Class<T> type, TypeAdapter<T> adapter) {
        adapters.put(type, adapter);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> get(Class<T> type) {
        return (TypeAdapter<T>) adapters.get(type);
    }

    public @NotNull Map<Class<?>, TypeAdapter<?>> getAdapters() {
        return adapters;
    }
}
