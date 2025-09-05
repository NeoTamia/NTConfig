package re.neotamia.config.naming;

/**
 * Strategy interface for transforming field names according to different naming conventions.
 * Used to convert Java field names to configuration file field names.
 */
@FunctionalInterface
public interface NamingStrategy {
    /**
     * Transform a field name according to this naming strategy.
     *
     * @param fieldName the original Java field name
     * @return the transformed name for use in configuration files
     */
    String transform(String fieldName);

    /**
     * Identity strategy - returns the field name unchanged.
     */
    NamingStrategy IDENTITY = fieldName -> fieldName;

    /**
     * camelCase strategy - returns the field name unchanged (Java default).
     */
    NamingStrategy CAMEL_CASE = fieldName -> fieldName;

    /**
     * PascalCase strategy - capitalizes the first letter.
     */
    NamingStrategy PASCAL_CASE = fieldName -> {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    };

    /**
     * snake_case strategy - converts camelCase to snake_case.
     */
    NamingStrategy SNAKE_CASE = fieldName -> {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    };

    /**
     * kebab-case strategy - converts camelCase to kebab-case.
     */
    NamingStrategy KEBAB_CASE = fieldName -> {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('-');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    };
}