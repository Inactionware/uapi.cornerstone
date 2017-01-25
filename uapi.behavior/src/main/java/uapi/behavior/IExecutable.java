/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

/**
 * The interface is used to generate IEventDrivenBehavior by EventBehavior annotation
 */
public interface IExecutable {

    /**
     * Return the root execution of the execution tree
     *
     * @return  The root execution
     */
    IExecution execution();
}
