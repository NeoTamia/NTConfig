package re.neotamia.config.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import re.neotamia.config.Serializer;
import re.neotamia.config.adapter.TypeAdapter;
import re.neotamia.config.registry.TypeAdapterRegistry;

import java.util.Set;

public class YamlSerializer implements Serializer {
    private final TypeAdapterRegistry registry;
    private final YAMLMapper mapper = new YAMLMapper()
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

    public YamlSerializer(TypeAdapterRegistry registry) {
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> String serialize(T obj) throws JsonProcessingException {
        TypeAdapter<T> adapter = registry.get((Class<T>) obj.getClass());
        if (adapter != null) {
            Object tree = adapter.serialize(obj);
            return mapper.writeValueAsString(tree);
        }
        return mapper.writeValueAsString(obj);
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) throws JsonProcessingException {
        T tree = mapper.readValue(data, type);
        TypeAdapter<T> adapter = registry.get(type);
        if (adapter != null) return adapter.deserialize(tree, type);
        return tree;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("yaml", "yml");
    }
}