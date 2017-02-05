/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.common.Repository;
import uapi.event.IEventBus;

/**
 * A responsible is used to fire behavior event and define specific behaviors based on behavior event
 */
public class Responsible implements IResponsible {

    private final String _name;
    private final Repository<String, IAction<?, ?>> _actionRepo;
    private final IEventBus _eventBus;

    Responsible(
            final String name,
            final IEventBus eventBus,
            final Repository<String, IAction<?, ?>> actionRepository
    ) {
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(eventBus, "eventBus");
        ArgumentChecker.required(actionRepository, "actionRepository");
        this._name = name;
        this._eventBus = eventBus;
        this._actionRepo = actionRepository;
    }

    @Override
    public String name() {
        return this._name;
    }

    @Override
    public IBehaviorBuilder newBehavior(final String topic) {
        ArgumentChecker.required(topic, "topic");
        Behavior behavior = new Behavior(this, this._actionRepo, BehaviorEvent.class);

        return null;
    }

    @Override
    public IBehaviorBuilder newBehavior(Class<?> type) {
        return null;
    }

    @Override
    public void on(BehaviorExecutingEventHandler handler) {

    }

    @Override
    public void on(BehaviorFinishedEventHandler handler) {

    }

    void publish(final Behavior behavior) {
        ArgumentChecker.required(behavior, "behavior");

    }
}
