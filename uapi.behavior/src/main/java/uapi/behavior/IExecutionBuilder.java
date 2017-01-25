/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.common.Functionals;

/**
 * The builder is used to create IExecution instance
 */
public interface IExecutionBuilder {

    /**
     * Set current execution result evaluation
     *
     * @param   evaluator
     *          The evaluator
     * @return  This execution builder
     */
    IExecutionBuilder when(Functionals.Evaluator evaluator);

    /**
     * Set next action when current execution result evaluation is satisfied.
     *
     * @param   name
     *          The action name
     * @return  Next execution builder
     */
    IExecutionBuilder then(String name);

    /**
     * Build whole execution tree
     *
     * @return  The root of execution tree
     */
    IExecution build();
}
