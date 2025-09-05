package re.neotamia.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the configuration version field.
 * This field will be used to track and manage configuration migrations.
 * <p>
 * The field can be of type String, int, or Integer.
 * String fields should contain either integer values (e.g., "1", "2") 
 * or semantic version strings (e.g., "1.0.0", "2.1.3").
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigVersion {
    /**
     * The default version to use if the field is not present in the loaded configuration.
     * This is used when loading configurations that don't have a version field yet.
     * 
     * @return the default version string
     */
    String defaultVersion() default "1";
}