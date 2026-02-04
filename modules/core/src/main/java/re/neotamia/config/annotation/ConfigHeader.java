package re.neotamia.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a header comment at the top of a configuration file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.RECORD_COMPONENT})
public @interface ConfigHeader {
    /**
     * The header to be added at the top of the configuration file.
     * Output as a comment in the file if the format supports it.
     *
     * @return the header comment, empty to disable
     */
    String value() default "";
}
