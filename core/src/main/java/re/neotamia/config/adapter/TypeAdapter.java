package re.neotamia.config.adapter;

import org.jetbrains.annotations.NotNull;

public interface TypeAdapter<T> {
    @NotNull Object serialize(@NotNull T obj);
    @NotNull T deserialize(@NotNull Object data, @NotNull Class<T> clazz);

    default @NotNull T deserialize(@NotNull Object data, @NotNull Class<T> clazz, @NotNull AdapterContext ctx) {
        return deserialize(data, clazz);
    }

    default @NotNull Object serialize(@NotNull T obj, @NotNull AdapterContext ctx) {
        return serialize(obj);
    }
}
