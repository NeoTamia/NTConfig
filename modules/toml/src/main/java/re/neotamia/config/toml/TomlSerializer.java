package re.neotamia.config.toml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import re.neotamia.config.Serializer;

import java.util.Set;

public class TomlSerializer implements Serializer {
    private final TomlMapper mapper = new TomlMapper();

    @Override
    public Object toTree(String data) throws JsonProcessingException {
        return mapper.readValue(data, Object.class);
    }

    @Override
    public String fromTree(Object tree) throws JsonProcessingException {
        return mapper.writeValueAsString(tree);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("toml");
    }
}
