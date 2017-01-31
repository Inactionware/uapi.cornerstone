/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.event.IEventBus;
import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Read js files and generate Responsible
 */
@Service
@Tag("BEHAVIOR")
public class ResponsibleRegistry implements IResponsibleRegistry {

    @Inject
    protected ILogger _logger;

    @Inject
    protected IEventBus _eventBus;

    private Map<String, IResponsible> _responsibles = new HashMap<>();

    @Override
    public IResponsible register(String name) {
        return null;
    }

    @Override
    public void unregister(String name) {

    }
}
