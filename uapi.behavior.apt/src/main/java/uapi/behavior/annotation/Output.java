package uapi.behavior.annotation;

import uapi.common.StringHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the specific parameter is used as action output.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Output {

    /**
     * Set the name of the output, the name will be used to received output from behavior execution context.
     * The parameter name will be used if the output name is not specified.
     *
     * @return  The name of output
     */
    String name() default StringHelper.EMPTY;
}
