/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.IBehaviorEvent;
import uapi.behavior.IEventDrivenBehavior;
import uapi.behavior.IExecutionContext;
import uapi.behavior.Scope;
import uapi.common.ArgumentChecker;
import uapi.event.IEventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of IExecutionContext
 */
public final class ExecutionContext implements IExecutionContext {

    private final IEventBus _eventBus;
    private final Map<Object, Object> _globalData;
    private final Map<Object, Object> _data;

    public ExecutionContext(final IEventBus eventBus) {
        ArgumentChecker.required(eventBus, "eventBus");
        this._eventBus = eventBus;
        this._globalData = new ConcurrentHashMap<>();
        this._data = new HashMap<>();
    }

    @Override
    public void fire(IBehaviorEvent event) {
        fire(event, false);
    }

    @Override
    public void fire(IBehaviorEvent event, boolean syncable) {
        event.attach(IEventDrivenBehavior.KEY_EVENT_CONTEXT, this._globalData);
        this._eventBus.fire(event, syncable);
    }

    @Override
    public void put(Object key, Object value, Scope scope) {
        ArgumentChecker.required(key, "key");
        ArgumentChecker.required(scope, "scope");

        if (scope == Scope.Behavior) {
            this._data.put(key, value);
        } else {
            this._globalData.put(key, value);
        }
    }

    @Override
    public void put(Map data, Scope scope) {
        ArgumentChecker.required(data, "data");
        ArgumentChecker.required(scope, "scope");

        if (scope == Scope.Behavior) {
            this._data.putAll(data);
        } else {
            this._globalData.putAll(data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key) {
        ArgumentChecker.required(key, "key");

        Object value;
        if (this._data.containsKey(key)) {
            value = this._data.get(key);
        } else {
            value = this._globalData.get(key);
        }
        return (T) value;
    }
}
