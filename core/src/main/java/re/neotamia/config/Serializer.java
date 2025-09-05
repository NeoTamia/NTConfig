package re.neotamia.config;

import java.util.Set;

public interface Serializer {
    /**
     * Parse raw text into a generic Java tree (Map/List/primitive) independent of the target type.
     */
    Object toTree(String data) throws Exception;

    /**
     * Render a generic Java tree (Map/List/primitive) into raw text in this serializer's format.
     */
    String fromTree(Object tree) throws Exception;

    /**
     * Render a CommentedTree with field comments into raw text in this serializer's format.
     * Default implementation ignores comments and delegates to fromTree.
     */
    default String fromCommentedTree(CommentedTree commentedTree) throws Exception {
        return fromTree(commentedTree.getData());
    }

    /**
     * Indicates whether this serializer supports comments.
     */
    default boolean supportsComments() {
        return false;
    }

    Set<String> getSupportedExtensions();
}
