/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.BehaviorFinishedEvent;
import uapi.behavior.BehaviorFinishedEventHandler;
import uapi.behavior.IBehaviorBuilder;
import uapi.behavior.IResponsible;
import uapi.behavior.annotation.BehaviorExecutingEventHandler;
import uapi.common.ArgumentChecker;

/**
 * A responsible is used to fire behavior event and define specific behaviors based on behavior event
 */
public class Responsible implements IResponsible {

    private String _name;

    Responsible(String name) {
        ArgumentChecker.required(name, "name");
        this._name = name;

        this.on((BehaviorFinishedEvent event) -> {

        });
    }

    @Override
    public String name() {
        return this._name;
    }

    @Override
    public IBehaviorBuilder newBehavior(String topic) {
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
}
