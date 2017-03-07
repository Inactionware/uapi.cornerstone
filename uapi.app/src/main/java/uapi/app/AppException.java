/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app;

import uapi.exception.ExceptionBuilder;
import uapi.exception.ParameterizedException;

/**
 * Exception class for uapi.app module
 */
public class AppException extends ParameterizedException {

    public static ApplicationExceptionBuilder builder() {
        return new ApplicationExceptionBuilder();
    }

    protected AppException(ApplicationExceptionBuilder builder) {
        super(builder);
    }

    public static final class ApplicationExceptionBuilder
            extends ExceptionBuilder<AppException, ApplicationExceptionBuilder> {

        private ApplicationExceptionBuilder() {
            super(AppErrors.CATEGORY, new AppErrors());
        }

        @Override
        protected AppException createInstance() {
            return new AppException(this);
        }
    }
}
