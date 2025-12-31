package re.neotamia.config.registry;

import re.neotamia.config.adapter.TypeAdapter;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapterRegistry {
    private final List<TypeAdapter<?, ?>> typeAdapters = new ArrayList<>();

    public void register(TypeAdapter<?, ?> adapter) {
        typeAdapters.add(adapter);
    }

    public List<TypeAdapter<?, ?>> getTypeAdapters() {
        return typeAdapters;
    }
}
