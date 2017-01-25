/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.IAction;
import uapi.behavior.IBehaviorRepository;
import uapi.behavior.IExecutionBuilder;
import uapi.behavior.IExecutionBuilderFactory;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * The implementation of IExecutionBuilderFactory
 */
@Service(IExecutionBuilderFactory.class)
@Tag("Behavior")
public class ExecutionBuilderFactory implements IExecutionBuilderFactory {

    @Inject
    protected IBehaviorRepository _behaviorRepo;

    @Override
    public IExecutionBuilder from(String name) {
        IAction action = this._behaviorRepo.find(name);
        if (action == null) {
            throw new GeneralException("Can't find action/behavior {} in the repo", name);
        }
        return new Execution(this._behaviorRepo, action);
    }
}
