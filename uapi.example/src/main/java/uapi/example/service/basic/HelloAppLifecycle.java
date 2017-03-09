/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.example.service.basic;

import uapi.app.IApplicationLifecycle;
import uapi.log.ILogger;
import uapi.service.IRegistry;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;

/**
 * Hello application lifecycle
 */
@Service(IApplicationLifecycle.class)
public class HelloAppLifecycle implements IApplicationLifecycle {

    @Inject
    protected IRegistry _registry;

    @Inject
    protected ILogger _logger;

    @Override
    public String getApplicationName() {
        return "HelloApp";
    }

    @Override
    public void onStarted() {
        IHello hello = this._registry.findService(IHello.class);
        hello.to("World");
    }

    @Override
    public void onStopped() {
        // do nothing
    }
}
