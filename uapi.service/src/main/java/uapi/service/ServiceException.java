/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.exception.ExceptionBuilder;
import uapi.exception.ParameterizedException;

/**
 * Exception for service module
 */
public class ServiceException extends ParameterizedException {

    public static ServiceExceptionBuilder builder() {
        return new ServiceExceptionBuilder(ServiceErrors.CATEGORY, new ServiceErrors());
    }

    protected ServiceException(ServiceExceptionBuilder builder) {
        super(builder);
    }

    public static final class ServiceExceptionBuilder extends ExceptionBuilder<ServiceException, ServiceExceptionBuilder> {

        public ServiceExceptionBuilder(int category, ServiceErrors errors) {
            super(category, errors);
        }

        @Override
        protected ServiceException createInstance() {
            return new ServiceException(this);
        }
    }
}
