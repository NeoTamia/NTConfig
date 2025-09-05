package re.neotamia.config.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import re.neotamia.config.CommentedTree;
import re.neotamia.config.Serializer;

import java.util.Map;
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
    public boolean supportsComments() {
        return true;
    }

    @Override
    public String fromCommentedTree(CommentedTree commentedTree) throws JsonProcessingException {
        String yamlContent = mapper.writeValueAsString(commentedTree.getData());
        
        if (!commentedTree.hasComments()) {
            return yamlContent;
        }
        
        // Post-process YAML to add comments
        return addCommentsToYaml(yamlContent, commentedTree.getFieldComments());
    }

    private String addCommentsToYaml(String yamlContent, Map<String, String> fieldComments) {
        String[] lines = yamlContent.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Look for field declarations (key:value or key: value)
            if (trimmed.contains(":") && !trimmed.startsWith("#")) {
                String fieldName = trimmed.split(":")[0].trim();
                String comment = fieldComments.get(fieldName);
                if (comment != null) {
                    // Add comment above the field with same indentation
                    String indent = line.substring(0, line.indexOf(line.trim()));
                    result.append(indent).append("# ").append(comment).append("\n");
                }
            }
            result.append(line).append("\n");
        }
        
        return result.toString();
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("yaml", "yml");
    }
}