/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.common.CollectionHelper;
import uapi.common.StringHelper;
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

    public static final int FOUND_CYCLE_DEPENDENCY              = 1;
    public static final int LOAD_EXTERNAL_SERVICE_FAILED        = 2;
    public static final int SERVICE_ACTIVE_TASK_TIMED_OUT       = 3;
    public static final int SERVICE_ACTIVATION_FAILED           = 4;
    public static final int MISSING_REQUIRED_DEPENDENCY         = 5;
    public static final int NOT_A_DEPENDENCY                    = 6;
    public static final int UNRESOLVED_DEPENDENCY               = 7;
    public static final int UNINJECTED_DEPENDENCY               = 8;
    public static final int UNSATISFIED_DEPENDENCY              = 9;
    public static final int SERVICE_CANNOT_BE_SATISFIED         = 10;
    public static final int UNACTIVATED_DEPENDENCY              = 11;
    public static final int UNSUPPORTED_SERVICE_HOLDER_STATE    = 12;
    public static final int DESTROYED_SERVICE                   = 13;
    public static final int UNSUPPORTED_DYNAMIC_INJECTION       = 14;
    public static final int MISSING_DEPENDENCY_OR_SERVICE       = 15;
    public static final int NO_SERVICE_TO_ACTIVATE              = 16;
    public static final int RESET_SERVICE_IS_DENIED             = 17;
    public static final int MULTIPLE_SERVICE_FOUND              = 18;
    public static final int NO_SERVICE_FOUND                    = 19;
    public static final int SERVICE_DEACTIVATION_TASK_TIMED_OUT = 20;
    public static final int NOT_A_PROTOTYPE_SERVICE             = 21;
    public static final int INSTANCE_SERVICE_REGISTER_FAILED    = 22;
    public static final int LOAD_MODULE_SERVICE_FILE_FAILED     = 23;
    public static final int NO_DEFAULT_CONSTRUCTOR_IN_SERVICE   = 24;
    public static final int CREATE_SERVICE_FAILED               = 25;
    public static final int SERVICE_IS_NOT_ISERVICE_TYPE        = 26;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(FOUND_CYCLE_DEPENDENCY, FoundCycleDependency.KEY);
        keyCodeMapping.put(LOAD_EXTERNAL_SERVICE_FAILED, LoadExternalServiceFailed.KEY);
        keyCodeMapping.put(SERVICE_ACTIVE_TASK_TIMED_OUT, ServiceActiveTaskTimedOut.KEY);
        keyCodeMapping.put(SERVICE_ACTIVATION_FAILED, ServiceActivationFailed.KEY);
        keyCodeMapping.put(MISSING_REQUIRED_DEPENDENCY, MissingRequiredDependency.KEY);
        keyCodeMapping.put(NOT_A_DEPENDENCY, NotDependency.KEY);
        keyCodeMapping.put(UNRESOLVED_DEPENDENCY, UnresolvedDependency.KEY);
        keyCodeMapping.put(UNINJECTED_DEPENDENCY, UninjectedDependency.KEY);
        keyCodeMapping.put(UNSATISFIED_DEPENDENCY, UnsatisfiedDependency.KEY);
        keyCodeMapping.put(SERVICE_CANNOT_BE_SATISFIED, ServiceCannotBeSatisfied.KEY);
        keyCodeMapping.put(UNACTIVATED_DEPENDENCY, UnactivatedDependency.KEY);
        keyCodeMapping.put(UNSUPPORTED_SERVICE_HOLDER_STATE, UnsupportedServiceHolderState.KEY);
        keyCodeMapping.put(DESTROYED_SERVICE, DestroyedService.KEY);
        keyCodeMapping.put(UNSUPPORTED_DYNAMIC_INJECTION, UnsupportedDynamicInjection.KEY);
        keyCodeMapping.put(MISSING_DEPENDENCY_OR_SERVICE, MissingDependencyOrService.KEY);
        keyCodeMapping.put(NO_SERVICE_TO_ACTIVATE, NoServiceToActivate.KEY);
        keyCodeMapping.put(RESET_SERVICE_IS_DENIED, ResetServiceIsDenied.KEY);
        keyCodeMapping.put(MULTIPLE_SERVICE_FOUND, MultipleServiceFound.KEY);
        keyCodeMapping.put(NO_SERVICE_FOUND, NoServiceFound.KEY);
        keyCodeMapping.put(SERVICE_DEACTIVATION_TASK_TIMED_OUT, ServiceDeactivationTaskTimedOut.KEY);
        keyCodeMapping.put(NOT_A_PROTOTYPE_SERVICE, NotAPrototypeService.KEY);
        keyCodeMapping.put(INSTANCE_SERVICE_REGISTER_FAILED, InstanceServiceRegisterFailed.KEY);
        keyCodeMapping.put(LOAD_MODULE_SERVICE_FILE_FAILED, LoadModuleServiceFileFailed.KEY);
        keyCodeMapping.put(NO_DEFAULT_CONSTRUCTOR_IN_SERVICE, NoDefaultConstructorInService.KEY);
        keyCodeMapping.put(CREATE_SERVICE_FAILED, CreateServiceFailed.KEY);
        keyCodeMapping.put(SERVICE_IS_NOT_ISERVICE_TYPE, ServiceIsNotIServiceType.KEY);
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
        private String _svcType;

        public ServiceActiveTaskTimedOut serviceId(final QualifiedServiceId serviceId) {
            this._svcId = serviceId;
            return this;
        }

        public ServiceActiveTaskTimedOut serviceType(final Object service) {
            this._svcType = service != null ? service.getClass().getCanonicalName() : StringHelper.EMPTY;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId, this._svcType };
        }
    }

    /**
     * The task for deactivate service is timed out - {}
     */
    public static final class ServiceDeactivationTaskTimedOut extends IndexedParameters<ServiceDeactivationTaskTimedOut> {

        private static final String KEY = "ServiceDeactivationTaskTimedOut";

        private QualifiedServiceId _svcId;
        private String _svcType;

        public ServiceDeactivationTaskTimedOut serviceId(final QualifiedServiceId serviceId) {
            this._svcId = serviceId;
            return this;
        }

        public ServiceDeactivationTaskTimedOut serviceType(final Object service) {
            this._svcType = service != null ? service.getClass().getCanonicalName() : StringHelper.EMPTY;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId, this._svcType };
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

    /**
     * The dependency {} of service {} is missing
     */
    public static final class MissingRequiredDependency extends IndexedParameters<MissingRequiredDependency> {

        private static final String KEY = "MissingRequiredDependency";

        private Dependency _dependency;
        private QualifiedServiceId _qSvcId;

        public MissingRequiredDependency dependency(Dependency dependency) {
            this._dependency = dependency;
            return this;
        }

        public MissingRequiredDependency qualifiedServiceId(QualifiedServiceId qualifiedServiceId) {
            this._qSvcId = qualifiedServiceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._dependency, this._qSvcId };
        }
    }

    /**
     * The service {} does not depend on service {}
     */
    public static final class NotDependency extends IndexedParameters<NotDependency> {

        private static final String KEY = "NotDependency";

        private QualifiedServiceId _thisQSvcId;
        private QualifiedServiceId _depQSvcId;

        public NotDependency thisServiceId(QualifiedServiceId qSvcId) {
            this._thisQSvcId = qSvcId;
            return this;
        }

        public NotDependency dependencyServiceId(QualifiedServiceId qSvcId) {
            this._depQSvcId = qSvcId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._thisQSvcId, this._depQSvcId };
        }
    }

    /**
     * The dependency {} of service {} is unresolved
     */
    public static final class UnresolvedDependency extends IndexedParameters<UnresolvedDependency> {

        private static final String KEY = "UnresolvedDependency";

        private QualifiedServiceId _thisQSvcId;
        private Dependency _dep;

        public UnresolvedDependency thisServiceId(QualifiedServiceId qSvcId) {
            this._thisQSvcId = qSvcId;
            return this;
        }

        public UnresolvedDependency dependency(Dependency dependency) {
            this._dep = dependency;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._dep, this._thisQSvcId };
        }
    }

    /**
     * The dependency {} of service {} is uninjected
     */
    public static final class UninjectedDependency extends IndexedParameters<UninjectedDependency> {

        private static final String KEY = "UninjectedDependency";

        private QualifiedServiceId _thisQSvcId;
        private Dependency _dep;

        public UninjectedDependency thisServiceId(QualifiedServiceId qSvcId) {
            this._thisQSvcId = qSvcId;
            return this;
        }

        public UninjectedDependency dependency(Dependency dependency) {
            this._dep = dependency;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._dep, this._thisQSvcId };
        }
    }

    /**
     * The dependency {} of service {} is unsatisfied
     */
    public static final class UnsatisfiedDependency extends IndexedParameters<UnsatisfiedDependency> {

        private static final String KEY = "UnsatisfiedDependency";

        private QualifiedServiceId _thisQSvcId;
        private Dependency _dep;

        public UnsatisfiedDependency thisServiceId(QualifiedServiceId qSvcId) {
            this._thisQSvcId = qSvcId;
            return this;
        }

        public UnsatisfiedDependency dependency(Dependency dependency) {
            this._dep = dependency;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[]{this._dep, this._thisQSvcId};
        }
    }

    /**
     * The service {} can not be satisfied
     */
    public static final class ServiceCannotBeSatisfied extends IndexedParameters<ServiceCannotBeSatisfied> {

        private static final String KEY = "ServiceCannotBeSatisfied";

        private QualifiedServiceId _svcId;

        public ServiceCannotBeSatisfied serviceId(QualifiedServiceId serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * The dependency {} of service {} is unactivated
     */
    public static final class UnactivatedDependency extends IndexedParameters<UnactivatedDependency> {

        private static final String KEY = "UnactivatedDependency";

        private QualifiedServiceId _thisQSvcId;
        private Dependency _dep;

        public UnactivatedDependency thisServiceId(QualifiedServiceId qSvcId) {
            this._thisQSvcId = qSvcId;
            return this;
        }

        public UnactivatedDependency dependency(Dependency dependency) {
            this._dep = dependency;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[]{this._dep, this._thisQSvcId};
        }
    }

    /**
     * Unsupported operation type for ServiceHolder - {}
     */
    public static final class UnsupportedServiceHolderState extends IndexedParameters<UnsupportedServiceHolderState> {

        private static final String KEY = "UnsupportedServiceHolderState";

        private String _opType;

        public UnsupportedServiceHolderState operationType(String operationType) {
            this._opType = operationType;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._opType };
        }
    }

    /**
     * The service is destroyed - {}
     */
    public static final class DestroyedService extends IndexedParameters<DestroyedService> {

        private static final String KEY = "DestroyedService";

        private QualifiedServiceId _svcId;

        public DestroyedService serviceId(QualifiedServiceId serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * The service does not support dynamic injection - {}
     */
    public static final class UnsupportedDynamicInjection extends IndexedParameters<UnsupportedDynamicInjection> {

        private static final String KEY = "UnsupportedDynamicInjection";

        private String _svcId;

        public UnsupportedDynamicInjection serviceId(String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * The unactivated service requires service dependency or its service holder
     */
    public static final class MissingDependencyOrService extends IndexedParameters<MissingDependencyOrService> {

        private static final String KEY = "MissingDependencyOrService";

        @Override
        public Object[] get() {
            return CollectionHelper.EMPTY_ARRAY;
        }
    }

    /**
     * There are no service to activate - {}
     */
    public static final class NoServiceToActivate extends IndexedParameters<NoServiceToActivate> {

        private static final String KEY = "NoServiceToActivate";

        private QualifiedServiceId _qSvcId;

        public NoServiceToActivate serviceId(QualifiedServiceId qid) {
            this._qSvcId = qid;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._qSvcId };
        }
    }

    /**
     * Reset service is denied - {}
     */
    public static final class ResetServiceIsDenied extends IndexedParameters<ResetServiceIsDenied> {

        private static final String KEY = "ResetServiceIsDenied";

        private QualifiedServiceId _qSvcId;

        public ResetServiceIsDenied serviceId(QualifiedServiceId qualifiedServiceId) {
            this._qSvcId = qualifiedServiceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._qSvcId };
        }
    }

    /**
     * Found multiple service by service id {}
     */
    public static final class MultipleServiceFound extends IndexedParameters<MultipleServiceFound> {

        private static final String KEY = "MultipleServiceFound";

        private String _svcId;

        public MultipleServiceFound serviceId(String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * Found 0 service by service id - {}
     */
    public static final class NoServiceFound extends IndexedParameters<NoServiceFound> {

        private static final String KEY = "NoServiceFound";

        private String _svcId;

        public NoServiceFound serviceId(String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * Error string template:
     *      The service is not a prototype service - {}
     */
    public static final class NotAPrototypeService extends IndexedParameters<NotAPrototypeService> {

        private static final String KEY = "NotAPrototypeService";

        private String _svcId;

        public NotAPrototypeService serviceId(String serviceId) {
            this._svcId = serviceId;
            return this;
        }

        public Object[] get() {
            return new Object[] { this._svcId };
        }
    }

    /**
     * Error string template:
     *      Register instance service fail - {}, prototype service - {}
     */
    public static final class InstanceServiceRegisterFailed extends IndexedParameters<InstanceServiceRegisterFailed> {

        private static final String KEY = "InstanceServiceRegisterFailed";

        private String _instSvcId;
        private String _prototypeSvcId;

        public InstanceServiceRegisterFailed instanceServiceId(String serviceId) {
            this._instSvcId = serviceId;
            return this;
        }

        public InstanceServiceRegisterFailed prototypeServiceId(String serviceId) {
            this._prototypeSvcId = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._instSvcId, this._prototypeSvcId };
        }
    }

    /**
     * Error string template:
     *      Load services from data file failed, module - {}
     */
    public static final class LoadModuleServiceFileFailed extends IndexedParameters<LoadModuleServiceFileFailed> {

        private static final String KEY = "LoadModuleServiceFileFailed";

        private String _moduleName;

        public LoadModuleServiceFileFailed moduleName(String name) {
            this._moduleName = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._moduleName };
        }
    }

    /**
     * Error string template:
     *      The service has no default constructor defined - {}, module - {}
     */
    public static final class NoDefaultConstructorInService extends IndexedParameters<NoDefaultConstructorInService> {

        private static final String KEY = "NoDefaultConstructorInService";

        private String _svcName;
        private String _moduleName;

        public NoDefaultConstructorInService serviceName(final String name) {
            this._svcName = name;
            return this;
        }

        public NoDefaultConstructorInService moduleName(final String name) {
            this._moduleName = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcName, this._moduleName };
        }
    }

    /**
     * Error string template:
     *      Create instance of service failed - {}, module - {}
     */
    public static final class CreateServiceFailed extends IndexedParameters<CreateServiceFailed> {

        private static final String KEY = "CreateServiceFailed";

        private String _svcName;
        private String _moduleName;

        public CreateServiceFailed serviceName(final String name) {
            this._svcName = name;
            return this;
        }

        public CreateServiceFailed moduleName(final String name) {
            this._moduleName = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcName, this._moduleName };
        }
    }

    /**
     * Error string template:
     *      The service {} is not instance of IService, module - {}
     */
    public static final class ServiceIsNotIServiceType extends IndexedParameters<ServiceIsNotIServiceType> {

        private static final String KEY = "ServiceIsNotIServiceType";

        private String _svcName;
        private String _moduleName;

        public ServiceIsNotIServiceType serviceName(final String name) {
            this._svcName = name;
            return this;
        }

        public ServiceIsNotIServiceType moduleName(final String name) {
            this._moduleName = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcName, this._moduleName };
        }
    }
}
