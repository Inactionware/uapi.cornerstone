/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.example.service.basic;

import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * IHell implementation
 */
@Service(IHello.class)
@Tag("Hello")
public class Hello implements IHello {

    @Inject
    protected ILogger _logger;

    @Override
    public void to(String name) {
        this._logger.info("Hello {} !", name);
    }
}
