package re.neotamia.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for tree data that can carry comment metadata for fields.
 * Used to pass comment information from NTConfig to serializers that support comments.
 */
public class CommentedTree {
    private final Object data;
    private final Map<String, String> fieldComments;

    public CommentedTree(Object data) {
        this.data = data;
        this.fieldComments = new LinkedHashMap<>();
    }

    public CommentedTree(Object data, Map<String, String> fieldComments) {
        this.data = data;
        this.fieldComments = new LinkedHashMap<>(fieldComments);
    }

    public Object getData() {
        return data;
    }

    public Map<String, String> getFieldComments() {
        return fieldComments;
    }

    public void addFieldComment(String fieldName, String comment) {
        if (comment != null && !comment.trim().isEmpty()) {
            fieldComments.put(fieldName, comment.trim());
        }
    }

    public String getFieldComment(String fieldName) {
        return fieldComments.get(fieldName);
    }

    public boolean hasComments() {
        return !fieldComments.isEmpty();
    }
}