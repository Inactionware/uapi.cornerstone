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
import uapi.event.IEventHandler;

/**
 * A Behavior which is driven by specific event
 */
public interface IEventDrivenBehavior<I extends IEvent>
        extends IBehavior<I, Void>, IEventHandler<I> {

    String KEY_EVENT_CONTEXT    = "__GlobalData";

    /**
     * The default method just invoke event handling method and return nothing
     *
     * @param   input
     *          Inputted data
     * @return  nothing
     */
//    @Override
//    default Void process(I input, IExecutionContext context) {
//        handle(input);
//        return null;
//    }

    /**
     * The event driven behavior returns null, so the output type
     * always Void
     *
     * @return  Void type
     */
    @Override
    default Class<Void> outputType() {
        return Void.class;
    }
}
