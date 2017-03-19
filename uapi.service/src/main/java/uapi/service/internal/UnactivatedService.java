package uapi.service.internal;

import uapi.common.ArgumentChecker;
import uapi.service.Dependency;
import uapi.service.ServiceErrors;
import uapi.service.ServiceException;

/**
 * Created by min on 2017/3/19.
 */
public class UnactivatedService {

    private final Dependency _dependency;
    private final ServiceHolder _svcHolder;

    private UnactivatedService _refBy = null;

    public UnactivatedService(
            final Dependency dependency,
            final ServiceHolder serviceHolder
    ) {
        this._dependency = dependency;
        this._svcHolder = serviceHolder;
    }

    public Dependency dependency() {
        return this._dependency;
    }

    public ServiceHolder serviceHolder() {
        return this._svcHolder;
    }

    public void referencedBy(UnactivatedService service) {
        this._refBy = service;
    }

    public UnactivatedService referencedBy() {
        return this._refBy;
    }

    public void checkCycleDependency() {
        UnactivatedService refSvc = this._refBy;
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

    @Override
    public boolean equals(final Object other) {
        if (! (other instanceof UnactivatedService)) {
            return false;
        }
        UnactivatedService otherSvc = (UnactivatedService) other;
        if (this._svcHolder == null || otherSvc._svcHolder == null) {
            return true;
        }
        return this._svcHolder.equals(otherSvc._svcHolder);
    }
}
