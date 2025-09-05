package re.neotamia.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for tree data that can carry comment metadata for fields.
 * Used to pass comment information from NTConfig to serializers that support comments.
 */
public class CommentedTree {
    private final Object data;
    private final @NotNull Map<String, String> fieldComments;
    private @Nullable String headerComment;

    public CommentedTree(Object data) {
        this(data, new LinkedHashMap<>(), null);
    }

    public CommentedTree(Object data, Map<String, String> fieldComments) {
        this(data, fieldComments, null);
    }

    public CommentedTree(Object data, Map<String, String> fieldComments, @Nullable String headerComment) {
        this.data = data;
        this.fieldComments = new LinkedHashMap<>(fieldComments);
        this.headerComment = headerComment;
    }

    public Object getData() {
        return data;
    }

    public @NotNull Map<String, String> getFieldComments() {
        return fieldComments;
    }

    public void addFieldComment(String fieldName, @Nullable String comment) {
        if (comment != null && !comment.trim().isEmpty()) {
            fieldComments.put(fieldName, comment.trim());
        }
    }

    public String getFieldComment(String fieldName) {
        return fieldComments.get(fieldName);
    }

    public @Nullable String getHeaderComment() {
        return headerComment;
    }

    public void setHeaderComment(@Nullable String headerComment) {
        this.headerComment = (headerComment != null && !headerComment.trim().isEmpty()) ? headerComment.trim() : null;
    }

    public boolean hasHeaderComment() {
        return headerComment != null && !headerComment.trim().isEmpty();
    }

    public boolean hasComments() {
        return !fieldComments.isEmpty() || hasHeaderComment();
    }
}