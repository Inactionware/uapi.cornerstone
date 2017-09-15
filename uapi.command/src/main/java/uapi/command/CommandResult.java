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
public final class CommandResult {

    private final boolean _isSuccess;
    private final String _msg;
    private final Throwable _ex;

    public static CommandResult success() {
        return new CommandResult(true, null, null);
    }

    public static CommandResult success(String message) {
        return new CommandResult(true, message, null);
    }

    public static CommandResult failure() {
        return new CommandResult(false, null, null);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, null);
    }

    public static CommandResult failure(Throwable exception) {
        return new CommandResult(false, null, exception);
    }

    public static CommandResult failure(String message, Throwable exception) {
        return new CommandResult(false, message, exception);
    }

    private CommandResult(
            final boolean isSuccess,
            final String message,
            final Throwable exception
    ) {
        this._isSuccess = isSuccess;
        this._msg = message;
        this._ex = exception;
    }

    /**
     * Indicate whether the command execution is successful or not.
     *
     * @return  True means success otherwise means failure
     */
    public boolean successful() {
        return this._isSuccess;
    }

    /**
     * The result message to describe command execution result.
     *
     * @return  The command execution result message
     */
    public String message() {
        return this._msg;
    }

    /**
     * The exception of this command execution when the command execution is failed.
     *
     * @return  The exception object or null if no exception for the command execution
     */
    public Throwable exception() {
        return this._ex;
    }
    
}
