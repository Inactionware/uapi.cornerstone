/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

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
     * @param   topic
     *          The event topic
     * @return  The behavior builder which can build a specific behavior which can handle specific event
     */
    IBehaviorBuilder newBehavior(String topic);

    /**
     * Create new behavior builder on specific input data type which can handled by the behavior.
     *
     * @param   type
     *          The data type which can be handled by the behavior
     * @return  The behavior builder
     */
    IBehaviorBuilder newBehavior(Class<?> type);

    void on(BehaviorExecutingEventHandler handler);

    void on(BehaviorFinishedEventHandler handler);
}
