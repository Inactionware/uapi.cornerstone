/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

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
    char shortName() default 0;

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
