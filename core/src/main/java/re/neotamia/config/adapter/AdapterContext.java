package re.neotamia.config.adapter;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carries path and field context during (de)serialization so adapters can be format-agnostic
 * and still access the field name or full dotted path when needed.
 */
public record AdapterContext(List<String> path, @Nullable Field field) {
    public AdapterContext(List<String> path, @Nullable Field field) {
        this.path = List.copyOf(path);
        this.field = field;
    }

    public static AdapterContext root() {
        return new AdapterContext(Collections.emptyList(), null);
    }

    public AdapterContext child(String key, @Nullable Field field) {
        List<String> newPath = new ArrayList<>(this.path);
        newPath.add(key);
        return new AdapterContext(newPath, field);
    }
}
