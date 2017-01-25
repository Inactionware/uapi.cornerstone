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
 * A IBehavior is responsible to process input event and output processed data based on specified event.
 *
 * @param   <I>
 *          Input event type
 * @param   <O>
 *          Output data type
 */
public interface IBehavior<I, O> extends IAction<I, O> {

//    void setExecution(IExecution execution);
}
