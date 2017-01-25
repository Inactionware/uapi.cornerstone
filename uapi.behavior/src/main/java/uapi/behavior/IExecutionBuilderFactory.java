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
 * A factory for IExecutionBuilder instance creating
 */
public interface IExecutionBuilderFactory {

    /**
     * Create an IExecutionBuilder instance by an action
     *
     * @param   name
     *          The action name
     * @return  IExecutionBuilder instance
     */
    IExecutionBuilder from(String name);

//    /**
//     * Create an IExecutionBuilder instance by action or behavior
//     *
//     * @param   name
//     *          The name of action or behavior
//     * @param   isBehavior
//     *          Indicate the name is action or behavior
//     * @return  IExecutionBuilder instance
//     */
//    IExecutionBuilder from(String name, boolean isBehavior);
}