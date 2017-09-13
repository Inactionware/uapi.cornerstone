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
 * Implementation holds command execution result.
 */
public interface ICommandResult {

    /**
     * Indicate whether the command execution is successful or not.
     *
     * @return  True means success otherwise means failure
     */
    boolean successful();

    /**
     * The result message to describe command execution result.
     *
     * @return  The command execution result message
     */
    String message();

    /**
     * The exception of this command execution when the command execution is failed.
     *
     * @return  The exception object or null if no exception for the command execution
     */
    Throwable exception();
    
}
