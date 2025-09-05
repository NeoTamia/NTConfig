package re.neotamia.config;

import java.util.Set;

public interface Serializer {
    <T> String serialize(T obj) throws Exception;
    <T> T deserialize(String data, Class<T> type) throws Exception;

    Set<String> getSupportedExtensions();
}
