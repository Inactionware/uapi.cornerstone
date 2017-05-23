/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import uapi.Tags;
import uapi.common.ArgumentChecker;
import uapi.config.Configuration;
import uapi.config.IConfigTracer;
import uapi.config.IConfigurable;
import uapi.service.ISatisfyHook;
import uapi.service.IServiceReference;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.Map;

/**
 * A Configurator manage all configuration and configurable config list and
 * set configuration into related configurable config.
 */
@Service({ ISatisfyHook.class, IConfigTracer.class })
@Tag(Tags.CONFIG)
class Configurator implements ISatisfyHook, IConfigTracer {

    private final Configuration _rootConfig;

    Configurator() {
        this._rootConfig = Configuration.createRoot();
    }

    @Override
    public boolean isSatisfied(IServiceReference serviceRef) {
        ArgumentChecker.notNull(serviceRef, "serviceRef");
        if (! (serviceRef.getService() instanceof IConfigurable)) {
            return true;
        }
        IConfigurable configurableSvc = (IConfigurable) serviceRef.getService();
        String[] paths = configurableSvc.getPaths();
        boolean isConfigured = true;
        for (String path : paths) {
            if (! this._rootConfig.bindConfigurable(path, serviceRef)) {
                isConfigured = false;
            }
        }
        return isConfigured;
    }

    @Override
    public void onChange(String path, Object config) {
        Configurator.this._rootConfig.setValue(path, config);
    }

    @Override
    public void onChange(Map<String, Object> configMap) {
        Configurator.this._rootConfig.setValue(configMap);
    }
}
