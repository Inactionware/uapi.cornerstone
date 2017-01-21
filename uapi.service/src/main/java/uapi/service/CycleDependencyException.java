/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.GeneralException;
import uapi.rx.Looper;
import uapi.service.internal.IServiceHolder;

import java.util.Stack;

/**
 * The exception represent some services have dependency cycle
 */
public class CycleDependencyException extends GeneralException {

    private final Stack<IServiceHolder> _dependencyStack;

    public CycleDependencyException(
            final Stack<IServiceHolder> dependencyStack
    ) {
        super("Found dependency cycle");
        this._dependencyStack = dependencyStack;
    }

    @Override
    public String getMessage() {
        StringBuilder msgBuffer = new StringBuilder(super.getMessage());
        Looper.on(this._dependencyStack)
                .foreach(dependency -> {
                    msgBuffer.append(" -> ");
                    msgBuffer.append(dependency.getQualifiedId());
                });
        return msgBuffer.toString();
    }
}
