/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.service.Dependency;
import uapi.service.IInstance;
import uapi.service.ISatisfyHook;
import uapi.service.QualifiedServiceId;

public class InstanceServiceHolder extends ServiceHolder {

    private final QualifiedServiceId _prototypeId;

    InstanceServiceHolder(
            final String from,
            final IInstance instance,
            final String serviceId,
            final Dependency[] dependencies,
            final ISatisfyHook satisfyHook) {
        super(from, instance, serviceId, dependencies, satisfyHook);
        this._prototypeId = new QualifiedServiceId(instance.prototypeId(), QualifiedServiceId.FROM_LOCAL);
    }

    QualifiedServiceId prototypeId() {
        return this._prototypeId;
    }
}
