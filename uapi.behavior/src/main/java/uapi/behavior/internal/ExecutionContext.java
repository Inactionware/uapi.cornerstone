/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.BehaviorEvent;
import uapi.behavior.BehaviorTraceEvent;
import uapi.behavior.IExecutionContext;
import uapi.behavior.Scope;
import uapi.common.ArgumentChecker;
import uapi.event.IEventBus;
import uapi.event.IEventFinishCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of IExecutionContext
 */
public class ExecutionContext implements IExecutionContext {

    private final Map<Object, Object> _globalData;
    private final Map<Object, Object> _data;
    private final Map<String, ActionOutputHolder> _outputs;
    private final IEventBus _eventBus;

    public ExecutionContext(final IEventBus eventBus) {
        ArgumentChecker.required(eventBus, "eventBus");
        this._globalData = new ConcurrentHashMap<>();
        this._data = new HashMap<>();
        this._outputs = new HashMap<>();
        this._eventBus = eventBus;
    }

    @Override
    public void put(
            final Object key,
            final Object value
    ) {
        put(key, value, Scope.BEHAVIOR);
    }

    @Override
    public void put(Object key, Object value, Scope scope) {
        ArgumentChecker.required(key, "key");
        ArgumentChecker.required(scope, "scope");

        if (scope == Scope.BEHAVIOR) {
            this._data.put(key, value);
        } else {
            this._globalData.put(key, value);
        }
    }

    @Override
    public void put(Map data, Scope scope) {
        ArgumentChecker.required(data, "data");
        ArgumentChecker.required(scope, "scope");

        if (scope == Scope.BEHAVIOR) {
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

    public void fireEvent(final BehaviorTraceEvent event) {
        ArgumentChecker.required(event, "event");
        this._eventBus.fire(event);
    }

    public void fireEvent(final BehaviorEvent event) {
        ArgumentChecker.required(event, "event");
        this._eventBus.fire(event);
    }

    public void fireEvent(final BehaviorEvent event, boolean sync) {
        ArgumentChecker.required(event, "event");
        this._eventBus.fire(event, sync);
    }

    public void fireEvent(final BehaviorEvent event, final IEventFinishCallback callback) {
        ArgumentChecker.required(event, "event");
        ArgumentChecker.required(callback, "callback");
        this._eventBus.fire(event, callback);
    }

    public void fireEvent(
            final BehaviorEvent event,
            final IEventFinishCallback callback,
            final boolean sync
    ) {
        this._eventBus.fire(event, callback, sync);
    }
}
