/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.exception.ExceptionBuilder;
import uapi.exception.ParameterizedException;

/**
 * The root exception for behavior framework
 */
public class BehaviorException extends ParameterizedException {

    public static BehaviorExceptionBuilder builder() {
        return new BehaviorExceptionBuilder();
    }

    protected BehaviorException(BehaviorExceptionBuilder builder) {
        super(builder);
        int i = 1;
    }

    public static final class BehaviorExceptionBuilder
            extends ExceptionBuilder<BehaviorException, BehaviorExceptionBuilder> {

        private BehaviorExceptionBuilder() {
            super(BehaviorErrors.CATEGORY, new BehaviorErrors());
        }

        @Override
        protected BehaviorException createInstance() {
            return new BehaviorException(this);
        }
    }
}
