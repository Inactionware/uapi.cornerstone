/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.event.IEvent;

/**
 * It represent as a domain object, it's responsibility to generate related behavior
 */
public interface IResponsible {

    /**
     * Get the name of the responsible
     *
     * @return  The name
     */
    String name();

    /**
     * Create new behavior builder on specific event topic.
     *
     * @param   name
     *          The name of behavior, the name must be unique
     * @param   topic
     *          The event topic
     * @return  The behavior builder which can build a specific behavior which can handle specific event
     * @throws  BehaviorException
     *          The name is used in registered behavior, error code see {@link BehaviorErrors.BEHAVIOR_ID_IS_USED}
     */
    IBehaviorBuilder newBehavior(
            String name,
            String topic
    ) throws BehaviorException;

    /**
     * Create new behavior builder on specific event topic.
     *
     * @param   name
     *          The name of behavior, the name must be unique
     * @param   eventType
     *          The event type
     * @param   topic
     *          The event topic
     * @return  The behavior builder which can build a specific behavior which can handle specific event
     * @throws  BehaviorException
     *          The name is used in registered behavior, error code see {@link BehaviorErrors.BEHAVIOR_ID_IS_USED}
     */
    IBehaviorBuilder newBehavior(
            String name,
            Class<? extends IEvent> eventType,
            String topic) throws BehaviorException;

    /**
     * Create new behavior builder on specific input data type which can handled by the behavior.
     *
     * @param   type
     *          The data type which can be handled by the behavior
     * @return  The behavior builder
     * @throws  BehaviorException
     *          The name is used in other behavior, error code see {@link BehaviorErrors.BEHAVIOR_ID_IS_USED}
     */
    IBehaviorBuilder newBehavior(String name, Class<?> type) throws BehaviorException;

    void on(BehaviorExecutingEventHandler handler);

    void on(BehaviorFinishedEventHandler handler);
}
