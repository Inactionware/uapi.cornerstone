/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.IIdentifiable;

/**
 * The IAction is minimum data handling unit in BEHAVIOR framework.
 * It process input data and output processed data.
 *
 * @param   <I>
 *          Input data type
 * @param   <O>
 *          Output data type
 */
public interface IAction<I, O> extends IIdentifiable<ActionIdentify> {

    /**
     * Process input data and output processed data
     *
     * @param   input
     *          Inputted data
     * @param   context
     *          The execution context
     * @return  Output data
     */
    O process(I input, IExecutionContext context);

    /**
     * Return input data type
     *
     * @return  input data type
     */
    Class<I> inputType();

    /**
     * Return output data type
     *
     * @return  output data type
     */
    Class<O> outputType();
}
