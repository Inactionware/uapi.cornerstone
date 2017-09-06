package uapi.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    /**
     * The option name
     *
     * @return  The option name
     */
    String name();

    /**
     * The short option name
     *
     * @return  The short option name
     */
    char shortName();

    /**
     * The option argument if the option is string type
     *
     * @return  The option argument or empty string
     */
    String argument() default "";

    /**
     * The option description
     *
     * @return  The option description
     */
    String description();
}
