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
import uapi.InvalidArgumentException;
import uapi.Tags;
import uapi.codegen.IGenerated;
import uapi.common.ArgumentChecker;
import uapi.log.ILogger;
import uapi.service.IServiceFactory;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

@Service
@Tag(Tags.LOG)
public class LoggerManager implements IServiceFactory<ILogger> {

    @Override
    public ILogger createService(Object serveFor) {
        ArgumentChecker.required(serveFor, "serveFor");

        if (serveFor instanceof IGenerated) {
            return new Logger(LoggerFactory.getLogger(((IGenerated) serveFor).originalType()));
        } else {
            return new Logger(LoggerFactory.getLogger(serveFor.getClass()));
        }
    }
}
