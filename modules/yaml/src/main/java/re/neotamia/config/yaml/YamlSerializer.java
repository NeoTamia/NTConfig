package re.neotamia.config.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import re.neotamia.config.Serializer;

import java.util.Set;

public class YamlSerializer implements Serializer {
    private final YAMLMapper mapper = new YAMLMapper()
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

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
        return Set.of("yaml", "yml");
    }
}