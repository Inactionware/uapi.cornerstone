/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.annotation;

import sun.reflect.generics.tree.VoidDescriptor;
import uapi.Type;
import uapi.common.StringHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation indicate the plain java class is a behavior action
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {

    /**
     * Name of the action
     *
     * @return  Action name
     */
    String value() default StringHelper.EMPTY;

    Class<?> dependsOn() default Void.class;

    String dependsOnName() default StringHelper.EMPTY;
}
