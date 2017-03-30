/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.exception.FileBasedExceptionErrors;
import uapi.exception.IndexedParameters;
import uapi.rx.Looper;
import uapi.service.internal.IServiceHolder;
import uapi.service.internal.ServiceHolder;
import uapi.service.internal.UnactivatedService;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Error definitions for service module
 */
public class ServiceErrors extends FileBasedExceptionErrors<ServiceException> {

    public static final int CATEGORY   = 0x0100;

    public static final int FOUND_CYCLE_DEPENDENCY      = 1;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(FOUND_CYCLE_DEPENDENCY, FoundCycleDependency.KEY);
    }

    @Override
    protected String getFile(ServiceException exception) {
        if (exception.category() == CATEGORY) {
            return "/serviceErrors.properties";
        }
        return null;
    }

    @Override
    protected String getKey(ServiceException e) {
        return keyCodeMapping.get(e.errorCode());
    }

    /**
     * Found cycle service dependency, dependency chain: {}
     */
    public static final class FoundCycleDependency extends IndexedParameters<FoundCycleDependency> {

        private static final String KEY = "FoundCycleDependency";

        private String _svcDepStr;

        public FoundCycleDependency serviceStack(UnactivatedService unactivatedService) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(unactivatedService.serviceId()).insert(0, " -> ");
            UnactivatedService refSvc = unactivatedService.referencedBy();
            while (refSvc != null) {
                buffer.insert(0, refSvc.serviceId()).insert(0, " -> ");
            }
            buffer.delete(0, 3);
            this._svcDepStr = buffer.toString();
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcDepStr };
        }
    }
}
