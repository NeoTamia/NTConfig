package re.neotamia.config;

public class NTConfigException extends RuntimeException {
    public NTConfigException(String message) {
        super(message);
    }

    public NTConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
