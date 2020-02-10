/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.common.ArgumentChecker;
import uapi.service.*;

import java.util.Map;

public class PrototypeServiceHolder extends ServiceHolder {

    PrototypeServiceHolder(
            final String from,
            final IPrototype service,
            final String serviceId,
            final Dependency[] dependencies,
            final ISatisfyHook satisfyHook) {
        super(from, service, serviceId, dependencies, satisfyHook);
    }

    IInstance newInstance(
            final Map<Object, Object> attributes
    ) {
        ArgumentChecker.required(attributes, "attributes");

        IPrototype prototype = (IPrototype) getService();
        return prototype.newInstance(attributes);
    }
}
