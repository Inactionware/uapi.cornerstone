/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class with this annotation will implement IEventDrivenBehavior interface
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBehavior {

    /**
     * The behavior's name
     *
     * @return  BEHAVIOR name
     */
    String name();

    /**
     * The topic of event which will be handled by this behavior
     *
     * @return  Event topic
     */
    String topic();

    /**
     * The type of event which will be handled by this behavior
     *
     * @return  Event class type
     */
    Class event();
}
