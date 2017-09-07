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
 * The implementation of the interface is used to execute single command.
 */
public interface ICommandExecutor {

    /**
     * Execute command and return the result of command.
     *
     * @return  The command execution result
     */
    ICommandResult execute();

    /**
     * Set command parameter value by specified parameter name to this command.
     *
     * @param   name The parameter name
     * @param   value The parameter value
     */
    void setParameter(String name, Object value);

    /**
     * Set boolean type option to this command.
     *
     * @param   name The option name
     */
    void setOption(String name);

    /**
     * Set string type option to this command.
     *
     * @param   name The option name
     * @param   argument The option argument value
     */
    void setOption(String name, String argument);

    /**
     * Set message output which is used to output information during command execution.
     *
     * @param   output The message output
     */
    void setMessageOutput(IMessageOutput output);
}
