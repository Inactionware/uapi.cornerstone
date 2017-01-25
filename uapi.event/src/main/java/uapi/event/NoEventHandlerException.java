/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event;

import uapi.GeneralException;

/**
 * The exception will be thrown when no handler can handle specific event
 */
public class NoEventHandlerException extends GeneralException {

    /**
     * Construct NoEventHandlerException instance
     *
     * @param   topic
     *          Event topic
     */
    public NoEventHandlerException(String topic) {
        super("There are no event handler for event topic - {}", topic);
    }
}
