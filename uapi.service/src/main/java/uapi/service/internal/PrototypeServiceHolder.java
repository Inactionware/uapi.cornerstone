/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.GeneralException;
import uapi.service.*;

import java.util.Map;

public class PrototypeServiceHolder extends ServiceHolder {
    PrototypeServiceHolder(String from, Object service, String serviceId, Dependency[] dependencies, ISatisfyHook satisfyHook) {
        super(from, service, serviceId, dependencies, satisfyHook);
    }

//    PrototypeServiceHolder(
//            final ServiceHolder prototypeSvcHolder,
//            final Map<String, ?> attributes
//    ) {
//        if (! prototypeSvcHolder.isActivated()) {
//            throw new GeneralException("The prototype service is not activated");
//        }
//        IPrototype prototype = (IPrototype) prototypeSvcHolder.getService();
//        Object instanceSvc = prototype.newInstance(attributes);
//        Dependency[] dependencies;
//        if (instanceSvc instanceof IInjectable) {
//            IInjectable injectableSvc = (IInjectable) instanceSvc;
//            dependencies = injectableSvc.getDependencies();
//        } else {
//            dependencies = new Dependency[0];
//        }
//        String from;
//        String svcId;
//        if (instanceSvc instanceof IService) {
//            IService svc = (IService) instanceSvc;
//            from = svc.
//        }
//        super(prototypeSvcHolder.getFrom(), instanceSvc, prototypeSvcHolder.getId(), dependencies, prototypeSvcHolder._satisfyHook);
//    }

    @Override
    public Object getService() {
        return ((IPrototype) super.getService()).newInstance(null);
    }

    protected void doInject(
            final ServiceHolder dependSvcHolder
    ) {
        Object injectedSvc;
        if (dependSvcHolder instanceof PrototypeServiceHolder) {
            injectedSvc = ((PrototypeServiceHolder) dependSvcHolder).getService();
        } else {
            injectedSvc = dependSvcHolder.getService();
        }

        Object instance = getService();
        if (! (instance instanceof IInjectable)) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.UNSUPPORTED_INJECTION)
                    .variables(getId())
                    .build();
        }
        ((IInjectable) instance).injectObject(new Injection(dependSvcHolder.getId(), injectedSvc));
    }
}
