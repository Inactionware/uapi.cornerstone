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
 * Implementation of this interface is used to output message during command execution.
 */
public interface IMessageOutput {

    /**
     * Output specific message.
     *
     * @param   message The message
     */
    void output(String message);
}
