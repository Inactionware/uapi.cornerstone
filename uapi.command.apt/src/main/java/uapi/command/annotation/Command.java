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

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * The parentPath command type
     *
     * @return  The parentPath command type
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
