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
 * Present an action execution
 */
public interface IExecution {

    /**
     * Execute the action by specific data and context
     *
     * @param   data
     *          The input data
     * @param   context
     *          The execution context
     * @return  The result of execution
     */
    Object execute(Object data, IExecutionContext context);

    /**
     * Check the last execution data to ensure which child execution should be executed
     * in next round.
     *
     * @param   data
     *          The data which generated in last execution
     * @return  The next execution or null
     */
    IExecution next(Object data);
}
