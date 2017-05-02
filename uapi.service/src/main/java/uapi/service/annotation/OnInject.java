package uapi.service.annotation;

import uapi.common.StringHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation indicate that new service is injected through this method when service is activated
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnInject {

    /**
     * Injected service's id
     *
     * @return  Service id
     */
    String value() default StringHelper.EMPTY;
}
