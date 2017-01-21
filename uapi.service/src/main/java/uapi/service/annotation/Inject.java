/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.annotation;

import uapi.common.StringHelper;
import uapi.service.QualifiedServiceId;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the annotated field can be injected into
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * Injected service's id
     *
     * @return  Service id
     */
    String value() default StringHelper.EMPTY;

    /**
     * Indicate where is the injected service from
     *
     * @return  Injected service from
     */
    String from() default QualifiedServiceId.FROM_ANY;
}
