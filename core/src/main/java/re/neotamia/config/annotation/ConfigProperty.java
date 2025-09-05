package re.neotamia.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty {
    /**
     * The description of the property.
     * Output as a comment above the property in the file if the format supports it.
     * @return the description
     */
    String value() default "";

    String name() default "";

    boolean exclude() default false;
}
