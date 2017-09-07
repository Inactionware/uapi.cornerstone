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
