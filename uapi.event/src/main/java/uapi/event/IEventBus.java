/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event;

/**
 * A event bus is used to dispatch event
 */
public interface IEventBus {

    /**
     * Fire a event which only contains topic, the event will be handled in async
     *
     * @param   topic
     *          The event topic
     */
    void fire(String topic);

    /**
     * Fire a event which only contains topic, the event will be handled by syncable tag
     *
     * @param   topic
     *          The event topic
     * @param   sync
     *          Synchronous or asynchronous to fire
     */
    void fire(String topic, boolean sync);

    /**
     * Fire event, the event will be handled in async
     *
     * @param   event
     *          Fired event
     */
    void fire(IEvent event);

    /**
     * Fire event, if the syncable set to true, the caller will be blocked until the event is handled
     *
     * @param   event
     *          Fired event
     * @param   sync
     *          Synchronous or asynchronous to fire
     */
    void fire(IEvent event, boolean sync);

    /**
     * Fire event, the callback will be invoked when the event is handled
     *
     * @param   event
     *          The event will be fired
     * @param   callback
     *          The callback which will be invoked when the event is handled
     */
    void fire(IEvent event, IEventFinishCallback callback);

    /**
     * Fire event, and block the thread until the callback is invoked when the event is
     *
     * @param   event
     *          Fired event
     * @param   callback
     *          The callback which will be invoked when the event is handled
     * @param   sync
     *          Synchronous or asynchronous to fire
     */
    void fire(IEvent event, IEventFinishCallback callback, boolean sync);

    /**
     * Register a event handler
     *
     * @param   eventHandler
     *          The event handler
     */
    void register(IEventHandler eventHandler);

    /**
     * Unregister a event handler
     *
     * @param   eventHandler
     *          The event handler
     * @return  True means operation is successful otherwise is failed
     */
    boolean unregister(IEventHandler eventHandler);
}
