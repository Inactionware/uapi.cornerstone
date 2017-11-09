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
 * Implementation of this interface can execution a command by specified command line which contains command name,
 * parameters and options.
 */
public interface ICommandRunner {

    /**
     * Execute command by specified command line.
     *
     * @param   commandLine The command line which contains command name, parameters and options
     * @param   output The message output object
     * @return  The command result
     * @throws  CommandException
     *          CommandErrors.COMMAND_NOT_FOUND Specified command was not found
     */
    CommandResult run(String commandLine, IMessageOutput output) throws CommandException;
}
