package uapi.service.internal;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import uapi.GeneralException;
import uapi.common.ArgumentChecker;
import uapi.common.CollectionHelper;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.state.IShifter;
import uapi.state.IStateListener;
import uapi.state.IStateTracer;
import uapi.state.StateCreator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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

        Looper.on(dependencies)
                .foreach(dependency -> this._dependencies.put(dependency, null));

        // Create state convert rule
        IShifter<ServiceState> stateShifter = (currentState, operation) -> {
            if (currentState == ServiceState.Destroyed) {
                throw new GeneralException("The service {} is destroyed", this._qualifiedSvcId);
            }

            ServiceState newState;
            switch(operation.type()) {
                case OP_RESOLVE:
                    innerResolve();
                    newState = ServiceState.Resolved;
                    break;
                case OP_INJECT:
                    if (currentState.value() < ServiceState.Resolved.value()) {
                        innerResolve();
                    }
                    innerInject();
                    newState = ServiceState.Injected;
                    break;
                case OP_SATISFY:
                    if (currentState.value() < ServiceState.Injected.value()) {
                        innerResolve();
                        innerInject();
                    }
                    innerSatisfy();
                    newState = ServiceState.Satisfied;
                    break;
                case OP_ACTIVATE:
                    if (currentState.value() < ServiceState.Satisfied.value()) {
                        innerResolve();
                        innerInject();
                        innerSatisfy();
                    }
                    innerActivate();
                    newState = ServiceState.Activated;
                    break;
                default:
                    throw new GeneralException("Unsupported operation type - {}", operation.type());
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

    public boolean isDependsOn(final String serviceId) {
        ArgumentChecker.notEmpty(serviceId, "serviceId");
        return isDependsOn(new QualifiedServiceId(serviceId, QualifiedServiceId.FROM_LOCAL));
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
            throw new GeneralException(
                    "The service {} does not depend on service {}", this._qualifiedSvcId, service.getQualifiedId());
        }
        this._dependencies.remove(dependency, null);
        this._dependencies.put(dependency, service);
    }

    /**
     * Retrieve unactivated services including all optional services
     *
     * @return  Unactivated service
     */
    public List<UnactivatedService> getUnactivatedServices() {
        return Looper.on(this._dependencies.entries())
                .filter(entry -> {
                    Dependency dependency = entry.getKey();
                    ServiceHolder svcHolder = entry.getValue();
                    if (svcHolder == null && ! dependency.isOptional()) {
                        return true;
                    }
                    if (svcHolder != null && ! svcHolder.isActivated()) {
                        return true;
                    }
                    return false;
                })
                .map(entry -> new UnactivatedService(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void injectNewDepdencies() {
        if (! isActivated()) {
            return;
        }
        if (! (this._svc instanceof IServiceLifecycle)) {
            throw new GeneralException("The service {} can't dynamic inject service", this.getId());
        }
        Looper.on(this._dependencies.values())
                .filter(dependSvcHolder -> ! this._injectedSvcs.contains(dependSvcHolder))
                .foreach(dependSvcHolder -> {
                    Object injectedSvc = dependSvcHolder.getService();
                    if (injectedSvc instanceof IServiceFactory) {
                        // Create service from service factory
                        injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
                    }
                    ((IServiceLifecycle) _svc).onServiceInjected(dependSvcHolder.getId(), dependSvcHolder.getService());
                    this._injectedSvcs.add(dependSvcHolder);
                });
    }

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
            throw new GeneralException("The dependency {} of service {} is missing", requiredSvc, this._qualifiedSvcId);
        }

        // Ensure all dependencies are resolved
        Dependency unresolvedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> ! entry.getValue().isResolved())
                .map(Map.Entry::getKey)
                .first(null);
        if (unresolvedSvc != null) {
            throw new GeneralException("The dependency {} of service is unresolved", unresolvedSvc, this._qualifiedSvcId);
        }
    }

    private void innerInject() {
        if (isInjected()) {
            return;
        }
        if (this._dependencies.size() > 0 && !(_svc instanceof IInjectable)) {
            throw new GeneralException("The service {} does not implement IInjectable interface", this._qualifiedSvcId);
        }

        // Ensure all dependencies are injected
        Dependency uninjectedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> ! entry.getValue().isInjected())
                .map(Map.Entry::getKey)
                .first(null);
        if (uninjectedSvc != null) {
            throw new GeneralException("The dependency {} of service is uninjected", uninjectedSvc, this._qualifiedSvcId);
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
                .filter(entry -> ! entry.getValue().isSatisfied())
                .map(Map.Entry::getKey)
                .first(null);
        if (unsatisfiedSvc != null) {
            throw new GeneralException("The dependency {} of service is unsatisfied", unsatisfiedSvc, this._qualifiedSvcId);
        }

        if (! this._satisfyHook.isSatisfied(ServiceHolder.this)) {
            throw new GeneralException("The service {} can'be satisfied", this._qualifiedSvcId);
        }
    }

    private void innerActivate() {
        if (isActivated()) {
            return;
        }

        // Ensure all dependencies are activated
        Dependency unactivatedSvc = Looper.on(this._dependencies.entries())
                .filter(entry -> ! entry.getValue().isActivated())
                .map(Map.Entry::getKey)
                .first(null);
        if (unactivatedSvc != null) {
            throw new GeneralException("The dependency {} of service is unactivated", unactivatedSvc, this._qualifiedSvcId);
        }

        if (_svc instanceof IInitial) {
            ((IInitial) _svc).init();
        }
    }

    private final class ServiceStateListener implements IStateListener<ServiceState> {

        private final ServiceHolder _svcHolder;

        private ServiceStateListener(final ServiceHolder serviceHolder) {
            this._svcHolder = serviceHolder;
        }

        @Override
        public void stateChanged(ServiceState oldState, ServiceState newState) {
            if (newState != ServiceState.Activated) {
                return;
            }

            if (! ServiceHolder.this.isActivated()) {
                throw new GeneralException("The service is activated - {}",
                        ServiceHolder.this._qualifiedSvcId);
            }

            // Inject activated dependency service
            Object injectedSvc = this._svcHolder.getService();
            if (injectedSvc instanceof IServiceFactory) {
                // Create service from service factory
                injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
            }
            ((IInjectable) _svc).injectObject(new Injection(this._svcHolder.getId(), injectedSvc));
            ServiceHolder.this._injectedSvcs.add(this._svcHolder);

            // Notify if the service need know some dependent service is injected
            if (ServiceHolder.this._svc instanceof IServiceLifecycle) {
                IServiceLifecycle svcLifecycle = (IServiceLifecycle) ServiceHolder.this._svc;
                svcLifecycle.onServiceInjected(this._svcHolder.getId(), this._svcHolder.getService());
            }

//            this._svcHolder.unsubscribe(this);
        }
    }
}
