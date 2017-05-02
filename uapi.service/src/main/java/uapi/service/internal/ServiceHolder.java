package uapi.service.internal;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import uapi.common.ArgumentChecker;
import uapi.common.CollectionHelper;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.state.IShifter;
import uapi.state.IStateTracer;
import uapi.state.StateCreator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hold service and provide dependency, lifecycle management
 */
public class ServiceHolder implements IServiceReference {

    private static final String OP_RESOLVE  = "resolve";
    private static final String OP_INJECT   = "inject";
    private static final String OP_SATISFY  = "satisfy";
    private static final String OP_ACTIVATE = "activate";

    private final Object _svc;
    private final String _svcId;
    private final String _from;
    private QualifiedServiceId _qualifiedSvcId;
    private final Multimap<Dependency, ServiceHolder> _dependencies;
    private final ISatisfyHook _satisfyHook;
    private final String[] _tags;

    private final List<ServiceHolder> _injectedSvcs = new LinkedList<>();
    private final IStateTracer<ServiceState> _stateTracer;

    ServiceHolder(
            final String from,
            final Object service,
            final String serviceId,
            final ISatisfyHook satisfyHook
    ) {
        this(from, service, serviceId, new Dependency[0], satisfyHook);
    }

    ServiceHolder(
            final String from,
            final Object service,
            final String serviceId,
            final Dependency[] dependencies,
            final ISatisfyHook satisfyHook
    ) {
        ArgumentChecker.notNull(from, "from");
        ArgumentChecker.notNull(service, "service");
        ArgumentChecker.notEmpty(serviceId, "serviceId");
        ArgumentChecker.notNull(dependencies, "dependencies");
        ArgumentChecker.notNull(satisfyHook, "satisfyHook");
        this._svc = service;
        this._svcId = serviceId;
        this._from = from;
        this._qualifiedSvcId = new QualifiedServiceId(serviceId, from);
        this._satisfyHook = satisfyHook;
        this._dependencies = LinkedListMultimap.create();
        if (service instanceof ITagged) {
            ITagged taggedSvc = (ITagged) service;
            this._tags = taggedSvc.getTags();
        } else {
            this._tags = new String[0];
        }

        Looper.on(dependencies)
                .foreach(dependency -> this._dependencies.put(dependency, null));

        // Create state convert rule
        IShifter<ServiceState> stateShifter = (currentState, operation) -> {
            if (currentState == ServiceState.Destroyed) {
                throw ServiceException.builder()
                        .errorCode(ServiceErrors.DESTROYED_SERVICE)
                        .variables(new ServiceErrors.DestroyedService()
                            .serviceId(this._qualifiedSvcId))
                        .build();
            }

            ServiceState newState;
            switch(operation.type()) {
                case OP_RESOLVE:
                    innerResolve();
                    newState = ServiceState.Resolved;
                    break;
                case OP_INJECT:
                    innerResolve();
                    innerInject();
                    newState = ServiceState.Injected;
                    break;
                case OP_SATISFY:
                    innerResolve();
                    innerInject();
                    innerSatisfy();
                    newState = ServiceState.Satisfied;
                    break;
                case OP_ACTIVATE:
                    innerResolve();
                    innerInject();
                    innerSatisfy();
                    innerActivate();
                    newState = ServiceState.Activated;
                    break;
                default:
                    throw ServiceException.builder()
                            .errorCode(ServiceErrors.UNSUPPORTED_SERVICE_HOLDER_STATE)
                            .variables(new ServiceErrors.UnsupportedServiceHolderState()
                                .operationType(operation.type()))
                            .build();
            }
            return newState;
        };
        this._stateTracer = StateCreator.createTracer(stateShifter, ServiceState.Unresolved);
    }

    ///////////////////////////////////////////////
    // Methods implements from IServiceReference //
    ///////////////////////////////////////////////

    @Override
    public String getId() {
        return this._svcId;
    }

    @Override
    public String getFrom() {
        return this._from;
    }

    @Override
    public QualifiedServiceId getQualifiedId() {
        return this._qualifiedSvcId;
    }

    @Override
    public Object getService() {
        return this._svc;
    }

    @Override
    public void notifySatisfied() {
        // todo:
    }

    ////////////////////
    // public methods //
    ////////////////////

    public String[] serviceTags() {
        return this._tags;
    }

    public void resolve() {
        this._stateTracer.shift(OP_RESOLVE);
    }

    public void inject() {
        this._stateTracer.shift(OP_INJECT);
    }

    public void satisfy() {
        this._stateTracer.shift(OP_SATISFY);
    }

    public void activate() {
        this._stateTracer.shift(OP_ACTIVATE);
    }

    public boolean isResolved() {
        return this._stateTracer.get().value() >= ServiceState.Resolved.value();
    }

    public boolean isInjected() {
        return this._stateTracer.get().value() >= ServiceState.Injected.value();
    }

    public boolean isSatisfied() {
        return this._stateTracer.get().value() >= ServiceState.Satisfied.value();
    }

    public boolean isActivated() {
        return this._stateTracer.get().value() >= ServiceState.Activated.value();
    }

    public boolean isDependsOn(QualifiedServiceId qualifiedServiceId) {
        ArgumentChecker.notNull(qualifiedServiceId, "qualifiedServiceId");
        return findDependencies(qualifiedServiceId) != null;
    }

    public boolean isDependsOn(final Dependency dependency) {
        ArgumentChecker.required(dependency, "dependency");
        Dependency dep = Looper.on(this._dependencies.keys())
                .filter(thisDependency -> dependency.getServiceId().isAssignTo(thisDependency.getServiceId()))
                .first(null);
        return dep != null;
    }

    public void setDependency(ServiceHolder service) {
        ArgumentChecker.notNull(service, "service");

        // remove null entry first
        Dependency dependency = findDependencies(service.getQualifiedId());
        if (dependency == null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.NOT_A_DEPENDENCY)
                    .variables(new ServiceErrors.NotDependency()
                        .thisServiceId(this._qualifiedSvcId)
                        .dependencyServiceId(service.getQualifiedId()))
                    .build();
        }
        this._dependencies.remove(dependency, null);
        this._dependencies.put(dependency, service);

        if (! isActivated()) {
            return;
        }
        // if service is activated then it should be notified
        if (! (this._svc instanceof IServiceLifecycle)) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNSUPPORTED_DYNAMIC_INJECTION)
                    .variables(new ServiceErrors.UnsupportedDynamicInjection()
                            .serviceId(this.getId()))
                    .build();
        }
        Object injectedSvc = service.getService();
        if (injectedSvc instanceof IServiceFactory) {
            // Create service from service factory
            injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
        }
        ((IServiceLifecycle) _svc).onDependencyInject(service.getId(), injectedSvc);
        this._injectedSvcs.add(service);
    }

    /**
     * Retrieve unactivated services including all optional services
     *
     * @return  Unactivated service
     */
    public List<UnactivatedService> getUnactivatedServices() {
        return Looper.on(this._dependencies.entries())
                .filter(entry -> {
                    ServiceHolder svcHolder = entry.getValue();
                    if (svcHolder == null) {
                        // Always try to load external service
                        return true;
                    }
                    return ! svcHolder.isActivated();
                })
                .map(entry -> new UnactivatedService(entry.getKey(), entry.getValue()))
                .toList();
    }

//    public void injectNewDependencies() {
//        if (! isActivated()) {
//            return;
//        }
//        if (! (this._svc instanceof IServiceLifecycle)) {
//            throw ServiceException.builder()
//                    .errorCode(ServiceErrors.UNSUPPORTED_DYNAMIC_INJECTION)
//                    .variables(new ServiceErrors.UnsupportedDynamicInjection()
//                        .serviceId(this.getId()))
//                    .build();
//        }
//        Looper.on(this._dependencies.values())
//                .filter(dependSvcHolder -> ! this._injectedSvcs.contains(dependSvcHolder))
//                .foreach(dependSvcHolder -> {
//                    Object injectedSvc = dependSvcHolder.getService();
//                    if (injectedSvc instanceof IServiceFactory) {
//                        // Create service from service factory
//                        injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
//                    }
//                    ((IServiceLifecycle) _svc).onDependencyInject(dependSvcHolder.getId(), injectedSvc);
//                    this._injectedSvcs.add(dependSvcHolder);
//                });
//    }

    /////////////////////
    // Private methods //
    /////////////////////

    private Dependency findDependencies(QualifiedServiceId qsId) {
        return Looper.on(this._dependencies.keySet())
                .filter(dependQsvcId -> qsId.isAssignTo(dependQsvcId.getServiceId()))
                .first(null);
    }

    private void innerResolve() {
        if (isResolved()) {
            return;
        }

        // Ensure unset dependencies is not required
        Dependency requiredSvc = Looper.on(_dependencies.entries())
                .filter(entry -> entry.getValue() == null)
                .filter(entry -> !((IInjectable) _svc).isOptional(entry.getKey().getServiceId().getId()))
                .map(Map.Entry::getKey)
                .first(null);
        if (requiredSvc != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.MISSING_REQUIRED_DEPENDENCY)
                    .variables(new ServiceErrors.MissingRequiredDependency()
                        .dependency(requiredSvc)
                        .qualifiedServiceId(this._qualifiedSvcId))
                    .build();
        }

        // Ensure all dependencies are activated
        Dependency unresolvedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isActivated())
                .map(Map.Entry::getKey)
                .first(null);
        if (unresolvedSvc != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNACTIVATED_DEPENDENCY)
                    .variables(new ServiceErrors.UnactivatedDependency()
                        .thisServiceId(this._qualifiedSvcId)
                        .dependency(unresolvedSvc))
                    .build();
        }
    }

    private void innerInject() {
        if (isInjected()) {
            return;
        }

        // Ensure all dependencies are activated
        Dependency uninjectedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isActivated())
                .map(Map.Entry::getKey)
                .first(null);
        if (uninjectedSvc != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNACTIVATED_DEPENDENCY)
                    .variables(new ServiceErrors.UnactivatedDependency()
                        .dependency(uninjectedSvc)
                        .thisServiceId(this._qualifiedSvcId))
                    .build();
        }

        // Inject depended service
        Looper.on(_dependencies.values())
                .filter(dependSvcHolder -> dependSvcHolder != null)
                .foreach(dependSvcHolder -> {
                    // if the service was injected before, it is not necessary to inject again
                    if (CollectionHelper.isStrictContains(this._injectedSvcs, dependSvcHolder)) {
                        return;
                    }
                    Object injectedSvc = dependSvcHolder.getService();
                    if (injectedSvc instanceof IServiceFactory) {
                        // Create service from service factory
                        injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
                    }
                    ((IInjectable) _svc).injectObject(new Injection(dependSvcHolder.getId(), injectedSvc));
                    this._injectedSvcs.add(dependSvcHolder);
                });
    }

    private void innerSatisfy() {
        if (isSatisfied()) {
            return;
        }

        // Ensure all dependencies are satisfied
        Dependency unsatisfiedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isActivated())
                .map(Map.Entry::getKey)
                .first(null);
        if (unsatisfiedSvc != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNACTIVATED_DEPENDENCY)
                    .variables(new ServiceErrors.UnactivatedDependency()
                        .thisServiceId(this._qualifiedSvcId)
                        .dependency(unsatisfiedSvc))
                    .build();
        }

        if (! this._satisfyHook.isSatisfied(ServiceHolder.this)) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.SERVICE_CANNOT_BE_SATISFIED)
                    .variables(new ServiceErrors.ServiceCannotBeSatisfied()
                        .serviceId(this._qualifiedSvcId))
                    .build();
        }
    }

    private void innerActivate() {
        if (isActivated()) {
            return;
        }

        // Ensure all dependencies are activated
        Dependency unactivatedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isActivated())
                .map(Map.Entry::getKey)
                .first(null);
        if (unactivatedSvc != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNACTIVATED_DEPENDENCY)
                    .variables(new ServiceErrors.UnactivatedDependency()
                        .thisServiceId(this._qualifiedSvcId)
                        .dependency(unactivatedSvc))
                    .build();
        }

        if (_svc instanceof IServiceLifecycle) {
            ((IServiceLifecycle) _svc).onActivate();
        }
    }
}
