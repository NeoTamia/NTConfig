package re.neotamia.config.toml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import re.neotamia.config.CommentedTree;
import re.neotamia.config.Serializer;

import java.util.Map;
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
    public boolean supportsComments() {
        return true;
    }

    @Override
    public String fromCommentedTree(CommentedTree commentedTree) throws JsonProcessingException {
        String tomlContent = mapper.writeValueAsString(commentedTree.getData());
        
        if (!commentedTree.hasComments()) {
            return tomlContent;
        }
        
        // Post-process TOML to add comments
        return addCommentsToToml(tomlContent, commentedTree.getFieldComments());
    }

    private String addCommentsToToml(String tomlContent, Map<String, String> fieldComments) {
        String[] lines = tomlContent.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Look for field declarations (key = value)
            if (trimmed.contains("=") && !trimmed.startsWith("#")) {
                String fieldName = trimmed.split("=")[0].trim();
                String comment = fieldComments.get(fieldName);
                if (comment != null) {
                    // Add comment above the field
                    result.append("# ").append(comment).append("\n");
                }
            }
            result.append(line).append("\n");
        }
        
        return result.toString();
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("toml");
    }
}
