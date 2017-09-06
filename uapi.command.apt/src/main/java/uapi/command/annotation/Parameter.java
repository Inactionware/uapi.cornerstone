package uapi.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * The parameter index of the command
     *
     * @return  The parameter index
     */
    int index();

    /**
     * The parameter is required or not
     *
     * @return  The parameter is required or not
     */
    boolean required() default true;

    /**
     * The parameter description
     *
     * @return  The parameter description
     */
    String description();
}
