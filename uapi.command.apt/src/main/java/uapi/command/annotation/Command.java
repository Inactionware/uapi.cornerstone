package uapi.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * The parent command type
     *
     * @return  The parent command type
     */
    Class<?> parent() default void.class;

    /**
     * The command name
     *
     * @return  The command name
     */
    String name();

    /**
     * The command description
     *
     * @return  The command description
     */
    String description();
}
