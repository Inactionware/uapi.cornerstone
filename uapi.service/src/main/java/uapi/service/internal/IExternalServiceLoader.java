/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.service.Dependency;

/**
 * Created by xquan on 4/6/2017.
 */
public interface IExternalServiceLoader {

    ServiceHolder loadService(Dependency dependency);
}
