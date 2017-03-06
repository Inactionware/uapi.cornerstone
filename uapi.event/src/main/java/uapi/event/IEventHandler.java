/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event;

import uapi.UapiException;

/**
 * A handler for specific event
 */
public interface IEventHandler<T extends IEvent> {

    /**
     * The topic of event which can be handled by this handler
     *
     * @return  The event handler
     */
    String topic();

    /**
     * Handle event
     *
     * @param   event
     *          The event
     * @throws  UapiException
     *          Handle event failed
     */
    void handle(T event) throws UapiException;
}
