/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

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
 * The ServiceHolder hold specific service with its id and dependencies
 */
public final class StatefulServiceHolder implements IServiceReference, IServiceHolder {

    private static final String OP_RESOLVE  = "resolve";
    private static final String OP_INJECT   = "inject";
    private static final String OP_SATISFY  = "satisfy";
    private static final String OP_ACTIVATE = "activate";

    private final Object _svc;
    private final String _svcId;
    private final String _from;
    private QualifiedServiceId _qualifiedSvcId;
    private final Multimap<Dependency, IServiceHolder> _dependencies;
    private final ISatisfyHook _satisfyHook;

    private final List<IServiceHolder> _injectedSvcs = new LinkedList<>();
    private final IStateTracer<ServiceState> _stateTracer;

    private ActivePolicy _activePolicy = ActivePolicy.LAZY;

    StatefulServiceHolder(
            final String from,
            final Object service,
            final String serviceId,
            final ISatisfyHook satisfyHook
    ) {
        this(from, service, serviceId, new Dependency[0], satisfyHook);
    }

    StatefulServiceHolder(
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
//        if (tryActivate(false)) {
//            return this._svc;
//        }
//        return null;
        return this._svc;
    }

    @Override
    public void notifySatisfied() {
        // TODO: Do we need this method?
    }

    ////////////////////////////////////////////
    // Methods implements from IServiceHolder //
    ////////////////////////////////////////////

    public boolean tryActivate() {
        return tryActivate(true);
    }

    public boolean tryActivate(final boolean throwException) {
        if (this._stateTracer.get().value() >= ServiceState.Activated.value()) {
            return true;
        }
        try {
            this._stateTracer.shift(OP_ACTIVATE);
        } catch (Exception ex) {
            if (throwException) {
                throw ex;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isDependsOn(final String serviceId) {
        ArgumentChecker.notEmpty(serviceId, "serviceId");
        return isDependsOn(new QualifiedServiceId(serviceId, QualifiedServiceId.FROM_LOCAL));
    }

    @Override
    public boolean isDependsOn(QualifiedServiceId qualifiedServiceId) {
        ArgumentChecker.notNull(qualifiedServiceId, "qualifiedServiceId");
        return findDependencies(qualifiedServiceId) != null;
    }

    @Override
    public void setDependency(IServiceHolder service) {
        ArgumentChecker.notNull(service, "service");

        Stack<IServiceHolder> dependencyStack = new Stack<>();
        checkCycleDependency(this, dependencyStack);

        // remove null entry first
        Dependency dependency = findDependencies(service.getQualifiedId());
        if (dependency == null) {
            throw new GeneralException(
                    "The service {} does not depend on service {}", this._qualifiedSvcId, service.getQualifiedId());
        }
        this._dependencies.remove(dependency, null);
        this._dependencies.put(dependency, service);

        if (this.isActivated()) {
            service.setActivePolicy(ActivePolicy.ASAP);
            service.subscribe(new ServiceStateListener(service));
        } else {
            service.setActivePolicy(this._activePolicy);
        }
    }

    @Override
    public List<Dependency> getUnsetDependencies() {
        return Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public void checkCycleDependency(
            final IServiceHolder svcToCheck,
            final Stack<IServiceHolder> dependencyStack
    ) throws CycleDependencyException {
        dependencyStack.push(this);
        Looper.on(this._dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getValue)
                .next(svcHolder -> {
                    if (svcHolder == svcToCheck) {
                        dependencyStack.push(svcHolder);
                        throw new CycleDependencyException(dependencyStack);
                    }
                })
                .foreach(dependency -> dependency.checkCycleDependency(svcToCheck, dependencyStack));

        IServiceHolder svcHolder = dependencyStack.pop();
        if (svcHolder != this) {
            throw new GeneralException("The last service item was not self - {}", this._qualifiedSvcId);
        }
    }

    @Override
    public void subscribe(IStateListener<ServiceState> listener) {
        this._stateTracer.subscribe(listener);
    }

    @Override
    public void unsubscribe(final IStateListener<ServiceState> listener) {
        this._stateTracer.unsubscribe(listener);
    }

    @Override
    public void setActivePolicy(ActivePolicy policy) {
        ArgumentChecker.required(policy, "policy");
        this._activePolicy = policy;
        if (this._activePolicy == ActivePolicy.ASAP) {
            this.tryActivate(false);
        }
    }

    private Dependency findDependencies(QualifiedServiceId qsId) {
        return Looper.on(this._dependencies.keySet())
                .filter(dependQsvcId -> qsId.isAssignTo(dependQsvcId.getServiceId()))
                .first(null);
    }

    @Override
    public void resolve() {
        this._stateTracer.shift(OP_RESOLVE);
    }

    @Override
    public void inject() {
        this._stateTracer.shift(OP_INJECT);
    }

    @Override
    public void satisfy() {
        this._stateTracer.shift(OP_SATISFY);
    }

    @Override
    public void activate() {
        this._stateTracer.shift(OP_ACTIVATE);
    }

    /////////////////////
    // Package methods //
    /////////////////////

    boolean isResolved() {
        return this._stateTracer.get().value() >= ServiceState.Resolved.value();
    }

    boolean isInjected() {
        return this._stateTracer.get().value() >= ServiceState.Injected.value();
    }

    boolean isSatisfied() {
        return this._stateTracer.get().value() >= ServiceState.Satisfied.value();
    }

    boolean isActivated() {
        return this._stateTracer.get().value() >= ServiceState.Activated.value();
    }

    /////////////////////
    // Private methods //
    /////////////////////

    private void innerResolve() {
        if (this._stateTracer.get().value() >= ServiceState.Resolved.value()) {
            return;
        }

        // Check dependencies is set or not
        Dependency unresolvedSvc = Looper.on(_dependencies.entries())
                .filter(entry -> entry.getValue() == null)
                .filter(entry -> !((IInjectable) _svc).isOptional(entry.getKey().getServiceId().getId()))
                .map(Map.Entry::getKey)
                .first(null);
        if (unresolvedSvc != null) {
            throw new GeneralException("The dependency {} of service {} is not resolved", unresolvedSvc, _qualifiedSvcId);
        }

        Looper.on(_dependencies.entries())
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getValue)
                .foreach(IServiceHolder::resolve);
    }

    private void innerInject() {
        if (this._stateTracer.get().value() >= ServiceState.Injected.value()) {
            return;
        }
        if (this._dependencies.size() > 0 && !(_svc instanceof IInjectable)) {
            throw new GeneralException("The service {} does not implement IInjectable interface", _qualifiedSvcId);
        }

        // Inject all dependent service
        Looper.on(_dependencies.entries())
                .map(Map.Entry::getValue)
                .filter(svcHolder -> svcHolder != null)
                .foreach(IServiceHolder::inject);

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
        if (this._stateTracer.get().value() >= ServiceState.Satisfied.value()) {
            return;
        }

        Looper.on(this._dependencies.entries())
                .map(Map.Entry::getValue)
                .filter(svcHolder -> svcHolder != null)
                .foreach(IServiceHolder::satisfy);

        if (! _satisfyHook.isSatisfied(StatefulServiceHolder.this)) {
            throw new GeneralException("The service {} can'be satisfied", this._qualifiedSvcId);
        }
    }

    private void innerActivate() {
        if (this._stateTracer.get().value() >= ServiceState.Activated.value()) {
            return;
        }

        Looper.on(this._dependencies.entries())
                .map(Map.Entry::getValue)
                .filter(svcHolder -> svcHolder != null)
                .foreach(IServiceHolder::activate);

        if (_svc instanceof IInitial) {
            ((IInitial) _svc).init();
        }
    }

    private final class ServiceStateListener implements IStateListener<ServiceState> {

        private final IServiceHolder _svcHolder;

        private ServiceStateListener(final IServiceHolder serviceHolder) {
            this._svcHolder = serviceHolder;
        }

        @Override
        public void stateChanged(ServiceState oldState, ServiceState newState) {
            if (newState != ServiceState.Activated) {
                return;
            }

            if (! StatefulServiceHolder.this.isActivated()) {
                throw new GeneralException("The service is activated - {}",
                        StatefulServiceHolder.this._qualifiedSvcId);
            }

            // Inject activated dependency service
            Object injectedSvc = this._svcHolder.getService();
            if (injectedSvc instanceof IServiceFactory) {
                // Create service from service factory
                injectedSvc = ((IServiceFactory) injectedSvc).createService(_svc);
            }
            ((IInjectable) _svc).injectObject(new Injection(this._svcHolder.getId(), injectedSvc));
            StatefulServiceHolder.this._injectedSvcs.add(this._svcHolder);

            // Notify if the service need know some dependent service is injected
            if (StatefulServiceHolder.this._svc instanceof IServiceLifecycle) {
                IServiceLifecycle svcLifecycle = (IServiceLifecycle) StatefulServiceHolder.this._svc;
                svcLifecycle.onServiceInjected(this._svcHolder.getId(), this._svcHolder.getService());
            }

            this._svcHolder.unsubscribe(this);
        }
    }
}
