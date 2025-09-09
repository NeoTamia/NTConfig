package re.neotamia.config.adapter;

import re.neotamia.nightconfig.core.serde.ValueDeserializer;
import re.neotamia.nightconfig.core.serde.ValueSerializer;

/**
 * Combines a {@link ValueSerializer} and a {@link ValueDeserializer} to a single type adapter.
 *
 * @param <T> type of the object to serialize/deserialize
 * @param <R> type of the serialized form
 */
public interface TypeAdapter<T, R> extends ValueSerializer<T, R>, ValueDeserializer<R, T> {}
