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

    String commandId();

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
    default void setParameter(String name, Object value) {
        throw CommandException.builder()
                .errorCode(CommandErrors.UNSUPPORTED_PARAMETER)
                .variables(new CommandErrors.UnsupportedParameter()
                        .parameterName(name)
                        .commandId(commandId()))
                .build();
    }

    /**
     * Set boolean type option to this command.
     *
     * @param   name The option name
     */
    default void setOption(String name) {
        throw CommandException.builder()
                .errorCode(CommandErrors.UNSUPPORTED_OPTION)
                .variables(new CommandErrors.UnsupportedOption()
                        .optionName(name)
                        .command(commandId()))
                .build();
    }

    /**
     * Set string type option to this command.
     *
     * @param   name The option name
     * @param   argument The option argument value
     */
    default void setOption(String name, String argument) {
        throw CommandException.builder()
                .errorCode(CommandErrors.UNSUPPORTED_OPTION)
                .variables(new CommandErrors.UnsupportedOption()
                        .optionName(name)
                        .command(commandId()))
                .build();
    }

    /**
     * Set message output which is used to output information during command execution.
     *
     * @param   output The message output
     */
    default void setMessageOutput(IMessageOutput output) { }
}
