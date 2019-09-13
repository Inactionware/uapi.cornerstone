package uapi.service.internal;

import uapi.GeneralException;
import uapi.common.IAwaiting;
import uapi.service.Dependency;
import uapi.service.ServiceErrors;
import uapi.service.ServiceException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An unactivated service hold a service holder which is not activated yet
 * The unactivated service also implement IAwaiting interface which can make other service
 * wait on the unactivated service until the unactivated service is activated
 */
public class UnactivatedService implements IAwaiting {

    private final Dependency _dependency;
    private ServiceHolder _svcHolder;

    private UnactivatedService _refBy = null;

    private final Lock _lock;
    private final Condition _condition;

    public UnactivatedService(
            final Dependency dependency,
            final ServiceHolder serviceHolder
    ) {
        if (dependency == null && serviceHolder == null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.MISSING_DEPENDENCY_OR_SERVICE)
                    .build();
        }
        this._dependency = dependency;
        this._svcHolder = serviceHolder;
        this._lock = new ReentrantLock();
        this._condition = this._lock.newCondition();
    }

    public Dependency dependency() {
        return this._dependency;
    }

    public ServiceHolder holder() {
        return this._svcHolder;
    }

    public void referencedBy(UnactivatedService service) {
        this._refBy = service;
    }

    public UnactivatedService referencedBy() {
        return this._refBy;
    }

    public void checkCycleDependency() {
        var refSvc = this._refBy;
        while (refSvc != null) {
            if (this.equals(refSvc)) {
                throw ServiceException.builder()
                        .errorCode(ServiceErrors.FOUND_CYCLE_DEPENDENCY)
                        .variables(new ServiceErrors.FoundCycleDependency()
                                .serviceStack(this).get())
                        .build();
            }
            refSvc = refSvc._refBy;
        }
    }

    public String serviceId() {
        if (this._svcHolder != null) {
            return this._svcHolder.getId();
        } else {
            return this._dependency.getServiceId().toString();
        }
    }

    public boolean isExternalService() {
        // only service holder is set to null not means it is external service, consider below case:
        // sometime one service depends on a local service but it was no imported by incorrect configuration
        return this._svcHolder == null && this._dependency.getServiceId().isExternalService();
    }

    public boolean isActivated() {
        return this._svcHolder != null && this._svcHolder.isActivated();
    }

    public Object service() {
        return this._svcHolder == null ? null : this._svcHolder.getService();
    }

    public List<UnactivatedService> getUnactivatedDependencies() {
        if (this._svcHolder == null) {
            return Collections.emptyList();
        }
        return this._svcHolder.getUnactivatedServices();
    }

    public void activate() {
        if (this._svcHolder == null) {
            return;
        }
        this._svcHolder.activate();
        this._lock.lock();
        try {
            if (this._svcHolder.isActivated()) {
                this._condition.signalAll();
            }
        } finally {
            this._lock.unlock();
        }
    }

    public void activate(ServiceHolder externalServiceHolder) {
        if (this._svcHolder != null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.RESET_SERVICE_IS_DENIED)
                    .variables(new ServiceErrors.ResetServiceIsDenied()
                        .serviceId(this._dependency.getServiceId()))
                    .build();
        }
        this._svcHolder = externalServiceHolder;
        activate();
    }

    @Override
    public boolean await(long waitTime) {
        this._lock.lock();
        try {
            if (this._svcHolder.isActivated()) {
                return true;
            } else {
                return this._condition.await(waitTime, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ex) {
            throw new GeneralException(ex);
        } finally {
            this._lock.unlock();
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (! (other instanceof UnactivatedService)) {
            return false;
        }
        UnactivatedService otherSvc = (UnactivatedService) other;
        if (this._svcHolder != null) {
            return this._svcHolder.equals(otherSvc._svcHolder);
        }
        if (this.isExternalService() && otherSvc.isExternalService()) {
            return this._dependency.equals(otherSvc._dependency);
        }
        return false;
    }
}
