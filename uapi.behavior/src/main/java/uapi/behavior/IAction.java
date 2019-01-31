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
 */
public interface IAction extends IIdentifiable<ActionIdentify> {

    /**
     * Process input data and output processed data
     *
     * @param   inputs
     *          Action inputs
     * @param   output
     *          Action outputs
     * @param   context
     *          The execution context
     * @return  Action result
     */
    default void process(Object[] inputs, ActionOutput output, IExecutionContext context) { }

    /**
     * Return input meta data
     *
     * @return  input data type
     */
    ActionInputMeta[] inputMetas();

    /**
     * Return output meta data
     *
     * @return  output data type
     */
    default ActionOutputMeta[] outputMetas() {
        return new ActionOutputMeta[0];
    }

    /**
     * Is this action anonymous or not
     *
     * @return  Return true if the action is anonymous otherwise return false
     */
    boolean isAnonymous();

    @Override
    ActionIdentify getId();
}
