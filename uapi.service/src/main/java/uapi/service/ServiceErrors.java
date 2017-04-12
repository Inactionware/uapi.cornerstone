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
import uapi.service.internal.UnactivatedService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Error definitions for service module
 */
public class ServiceErrors extends FileBasedExceptionErrors<ServiceException> {

    public static final int CATEGORY   = 0x0100;

    public static final int FOUND_CYCLE_DEPENDENCY          = 1;
    public static final int LOAD_EXTERNAL_SERVICE_FAILED    = 2;
    public static final int SERVICE_ACTIVE_TASK_TIMED_OUT   = 3;
    public static final int SERVICE_ACTIVATION_FAILED       = 4;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(FOUND_CYCLE_DEPENDENCY, FoundCycleDependency.KEY);
        keyCodeMapping.put(LOAD_EXTERNAL_SERVICE_FAILED, LoadExternalServiceFailed.KEY);
        keyCodeMapping.put(SERVICE_ACTIVE_TASK_TIMED_OUT, ServiceActiveTaskTimedOut.KEY);
        keyCodeMapping.put(SERVICE_ACTIVATION_FAILED, ServiceActivationFailed.KEY);
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

        public FoundCycleDependency serviceStack(final UnactivatedService unactivatedService) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(unactivatedService.serviceId()).insert(0, " >> ");
            UnactivatedService refSvc = unactivatedService.referencedBy();
            while (refSvc != null) {
                buffer.insert(0, refSvc.serviceId()).insert(0, " >> ");
                refSvc = refSvc.referencedBy();
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

    /**
     * Load external service but it return nothing - {}
     */
    public static final class LoadExternalServiceFailed extends IndexedParameters<LoadExternalServiceFailed> {

        private static final String KEY = "LoadExternalServiceFailed";

        private String _svcId;

        public LoadExternalServiceFailed serviceId(final String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * The task for activate service {} is timed out
     */
    public static final class ServiceActiveTaskTimedOut extends IndexedParameters<ServiceActiveTaskTimedOut> {

        private static final String KEY = "ServiceActiveTaskTimedOut";

        private QualifiedServiceId _svcId;

        public ServiceActiveTaskTimedOut serviceId(final QualifiedServiceId serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * The service activation is failed - {}
     */
    public static final class ServiceActivationFailed extends IndexedParameters<ServiceActivationFailed> {

        private static final String KEY = "ServiceActivationFailed";

        private String _svcId;

        public ServiceActivationFailed serviceId(final String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }
}
