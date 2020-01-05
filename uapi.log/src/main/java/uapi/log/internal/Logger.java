/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.log.internal;

import org.slf4j.LoggerFactory;
import uapi.service.Tags;
import uapi.common.StringHelper;
import uapi.log.ILogger;
import uapi.service.GenericAttributes;
import uapi.service.annotation.helper.ServiceType;
import uapi.service.annotation.Attribute;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

@Service(value=ILogger.class, type = ServiceType.Prototype)
@Tag(Tags.LOG)
public class Logger implements ILogger {

    org.slf4j.Logger _slfLogger;

    @Attribute(GenericAttributes.SERVE_FOR)
    protected String _serveFor;

    private org.slf4j.Logger getLogger() {
        if (this._slfLogger == null) {
            this._slfLogger = LoggerFactory.getLogger(this._serveFor);
        }
        return this._slfLogger;
    }

    @Override
    public void trace(String message, Object... parameters) {
        getLogger().trace(message, parameters);
    }

    @Override
    public void debug(String message, Object... parameters) {
        getLogger().debug(message, parameters);
    }

    @Override
    public void info(String message, Object... parameters) {
        getLogger().info(message, parameters);
    }

    @Override
    public void warn(String message, Object... parameters) {
        getLogger().warn(message, parameters);
    }

    @Override
    public void warn(Throwable t) {
        warn(t, t.getMessage());
    }

    @Override
    public void warn(Throwable t, String message, Object... parameters) {
        getLogger().warn(StringHelper.makeString(message, parameters), t);
    }

    @Override
    public void error(String message, Object... parameters) {
        getLogger().error(message, parameters);
    }

    @Override
    public void error(Throwable t) {
        error(t, t.getMessage());
    }

    @Override
    public void error(Throwable t, String message, Object... parameters) {
        getLogger().error(StringHelper.makeString(message, parameters), t);
    }
}
