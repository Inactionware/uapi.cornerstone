/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

/**
 * The class holds command parameter meta information.
 */
public interface IParameterMeta {

    /**
     * The name of the command parameter.
     *
     * @return  The name of command parameter
     */
    String name();

    /**
     * Is the parameter is required for the command.
     *
     * @return  True means the parameter is required to the command otherwise it is optional
     */
    boolean required();

    /**
     * Description of the command parameter.
     *
     * @return  Description of the command parameter
     */
    String description();
}
