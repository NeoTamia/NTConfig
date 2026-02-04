package re.neotamia.config;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.serde.SerdeException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for formatting serialization and deserialization error messages.
 */
public final class SerdeErrorFormatter {
    private static final Pattern FIELD_PATTERN = Pattern.compile("field `([^`]+)`");

    private SerdeErrorFormatter() {
        // Utility class
    }

    /**
     * Constructs a detailed error message for serialization/deserialization issues, providing information
     * about the root class, the field path, and the specific cause of the failure.
     *
     * @param action          A description of the action being performed, such as "serialize" or "deserialize".
     * @param rootClassName   The fully qualified name of the root configuration class involved in the process.
     * @param exception       The {@link SerdeException} containing details about the serialization/deserialization error.
     * @return A non-null string describing the failure, including the action, root class, field path, and cause.
     */
    static @NotNull String buildSerdeMessage(@NotNull String action, @NotNull String rootClassName, @NotNull SerdeException exception) {
        List<String> fieldPath = new ArrayList<>();
        List<String> classPath = new ArrayList<>();
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (!(current instanceof SerdeException) || current.getMessage() == null) {
                continue;
            }
            Matcher matcher = FIELD_PATTERN.matcher(current.getMessage());
            if (matcher.find()) {
                String fieldDescriptor = matcher.group(1);
                FieldInfo fieldInfo = parseFieldDescriptor(fieldDescriptor);
                fieldPath.add(fieldInfo.fieldName());
                classPath.add(fieldInfo.classPath());
            }
        }
        String path = fieldPath.isEmpty() ? "<unknown>" : String.join(".", fieldPath);
        String classPathDisplay = classPath.isEmpty() ? "<unknown>" : String.join(" -> ", classPath);
        String rootCause = findRootCauseMessage(exception);
        String simpleRoot = rootClassName.substring(rootClassName.lastIndexOf('.') + 1);
        return "Failed to " + action + " configuration class " + simpleRoot + " (" + rootClassName + ")"
                + ". Field path: " + path + " (" + classPathDisplay + "). Cause: " + rootCause;
    }

    /**
     * Traverses the causal chain of the given throwable to determine the root cause
     * and returns a descriptive message for it. If the root cause has no message
     * or its message is blank, the class name of the root cause is returned instead.
     *
     * @param throwable the throwable to analyze for its root cause; must not be null
     * @return the message of the root cause, or the class name if the message is null or blank
     */
    static @NotNull String findRootCauseMessage(@NotNull Throwable throwable) {
        Throwable current = throwable;
        Throwable last = throwable;
        while (current != null) {
            last = current;
            current = current.getCause();
        }
        String message = last.getMessage();
        return message == null || message.isBlank() ? last.getClass().getName() : message;
    }

    /**
     * Parses the provided field descriptor to extract detailed information about the field.
     *
     * @param fieldDescriptor A non-null string representing the field descriptor. It typically consists of a class name,
     *                        followed by a field name, separated by a dot. It may also include additional whitespace
     *                        and descriptive parts that are trimmed and processed.
     * @return A {@link FieldInfo} object containing the parsed field information, including a formatted field identifier
     *         and the raw field name. If the input descriptor lacks a clear dot-based separation of class and field,
     *         it returns the entire descriptor as both the identifier and field name.
     */
    static @NotNull FieldInfo parseFieldDescriptor(@NotNull String fieldDescriptor) {
        String[] parts = fieldDescriptor.trim().split("\\s+");
        String lastToken = parts.length == 0 ? fieldDescriptor : parts[parts.length - 1];
        int lastDot = lastToken.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= lastToken.length() - 1) {
            return new FieldInfo(lastToken, lastToken);
        }
        String className = lastToken.substring(0, lastDot);
        String fieldName = lastToken.substring(lastDot + 1);
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return new FieldInfo(simpleClassName + "." + fieldName, fieldName);
    }

    private record FieldInfo(@NotNull String classPath, @NotNull String fieldName) {}
}
