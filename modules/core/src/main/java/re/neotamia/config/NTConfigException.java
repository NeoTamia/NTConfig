package re.neotamia.config;

/**
 * Exception thrown to indicate errors related to NTConfig operations.
 */
public class NTConfigException extends RuntimeException {
    /**
     * Constructs a new NTConfigException with the specified detail message.
     *
     * @param message the detail message, which provides more information about the error.
     */
    public NTConfigException(String message) {
        super(message);
    }

    /**
     * Constructs a new NTConfigException with the specified detail message and cause.
     *
     * @param message the detail message, which provides more information about the error.
     * @param cause   the cause of the exception, which can be used to identify the underlying issue.
     */
    public NTConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
