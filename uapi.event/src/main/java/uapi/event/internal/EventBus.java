/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event.internal;

import uapi.GeneralException;
import uapi.service.Tags;
import uapi.common.ArgumentChecker;
import uapi.common.IAttributed;
import uapi.common.IntervalTime;
import uapi.config.annotation.Config;
import uapi.config.internal.IntervalTimeParser;
import uapi.event.*;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.IServiceLifecycle;
import uapi.service.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Event bus implementation
 */
@Service(IEventBus.class)
@Tag(Tags.EVENT)
public class EventBus implements IEventBus, IServiceLifecycle {

    private static final IntervalTime DEFAULT_AWAIT_TIME = IntervalTime.parse("100s");

    @Config(path="event.await-time", parser=IntervalTimeParser.class, optional=true)
    IntervalTime _awaitTime;

    @Inject
    @Optional
    protected List<IEventHandler> _eventHandlers = new CopyOnWriteArrayList<>();

    @Inject
    protected ILogger _logger;

    private ForkJoinPool _fjPoll = new ForkJoinPool();

    @OnActivate
    protected void init() {
        if (this._awaitTime == null) {
            this._awaitTime = DEFAULT_AWAIT_TIME;
        }
    }

    @OnDeactivate
    public void destroy() throws InterruptedException {
        this._fjPoll.shutdown();
        this._fjPoll.awaitTermination(this._awaitTime.seconds(), TimeUnit.SECONDS);
    }

    @Override
    public void fire(
            final String topic
    ) {
        this.fire(new PlainEvent(topic));
    }

    @Override
    public void fire(
            final String topic,
            boolean syncable
    ) {
        this.fire(new PlainEvent(topic), syncable);
    }

    @Override
    public void fire(
            final IEvent event
    ) {
        fire(event, false);
    }

    @Override
    public void fire(
            final IEvent event,
            final boolean syncable
    ) {
        ArgumentChecker.required(event, "event");

        List<IEventHandler> handlers = findHandlers(event);
        if (handlers.size() == 0) {
            this._logger.warn("There are no event handler for event topic - {}", event.topic());
            return;
        }

        HandleEventAction action = new HandleEventAction(handlers, event, syncable);
        if (syncable) {
            ForkJoinTask<Void> task = this._fjPoll.submit(action);
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new GeneralException(ex);
            }
        } else {
            this._fjPoll.submit(action);
        }
    }

    @Override
    public <T extends IEvent> void fire(
            final T event,
            final IEventFinishCallback<T> callback
    ) {
        ArgumentChecker.required(event, "event");
        ArgumentChecker.required(callback, "callback");

        List<IEventHandler> handlers = findHandlers(event);
        if (handlers.size() == 0) {
            this._logger.warn("There are no event handler for event topic - {}", event.topic());
            return;
        }

        HandleEventAction action = new HandleEventAction(handlers, event, callback);
        this._fjPoll.submit(action);
    }

    @Override
    public <T extends IEvent> void fire(
            final T event,
            final IEventFinishCallback<T> callback,
            final boolean sync
    ) {
        ArgumentChecker.required(event, "event");
        ArgumentChecker.required(callback, "callback");

        List<IEventHandler> handlers = findHandlers(event);
        if (handlers.size() == 0) {
            this._logger.warn("There are no event handler for event topic - {}", event.topic());
            return;
        }

        HandleEventAction action = new HandleEventAction(handlers, event, callback);
        if (sync) {
            ForkJoinTask<Void> task = this._fjPoll.submit(action);
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new GeneralException(ex);
            }
        } else {
            this._fjPoll.submit(action);
        }
    }

    @Override
    public void register(IEventHandler eventHandler) {
        ArgumentChecker.required(eventHandler, "eventHandler");
        this._eventHandlers.add(eventHandler);
    }

    @Override
    public boolean unregister(IEventHandler eventHandler) {
        ArgumentChecker.required(eventHandler, "eventHandler");
        return this._eventHandlers.remove(eventHandler);
    }

    private List<IEventHandler> findHandlers(IEvent event) {
        String topic = event.topic();
        List<IEventHandler> handlers = Looper.on(this._eventHandlers)
                .filter(handler -> handler.topic().equals(topic))
                .toList();
        if (event instanceof IAttributed) {
            final IAttributed attributed = (IAttributed) event;
            handlers = Looper.on(handlers)
                    .filter(handler -> (handler instanceof IAttributedEventHandler))
                    .map(handler -> (IAttributedEventHandler) handler)
                    .filter(handler -> attributed.contains(handler.getAttributes()))
                    .map(handler -> (IEventHandler) handler)
                    .toList();
        }
        return handlers;
    }

    @Override
    public void onDependencyInject(
            final String serviceId,
            final Object service) {
        if (IEventHandler.class.getCanonicalName().equals(serviceId) && service instanceof IEventHandler) {
            this._eventHandlers.add((IEventHandler) service);
        } else {
            throw new GeneralException(
                    "Unsupported dependency injection - {}, {}", service, service.getClass().getCanonicalName());
        }
    }

    private class HandleEventAction extends RecursiveAction {

        private final IEvent _event;
        private final List<IEventHandler> _handlers;
        private final WaitType _waitType;
        private final IEventFinishCallback _finCallback;

        private HandleEventAction(IEventHandler handler, IEvent event) {
            this._event = event;
            this._handlers = new LinkedList<>();
            this._handlers.add(handler);
            this._waitType = WaitType.NO_WAIT;
            this._finCallback = null;
        }

        private HandleEventAction(List<IEventHandler> handlers, IEvent event, boolean blocked) {
            this._event = event;
            this._handlers = handlers;
            if (blocked) {
                this._waitType = WaitType.BLOCKED;
            } else {
                this._waitType = WaitType.NO_WAIT;
            }
            this._finCallback = null;
        }

        private HandleEventAction(List<IEventHandler> handlers, IEvent event, IEventFinishCallback callback) {
            this._event = event;
            this._handlers = handlers;
            this._waitType = WaitType.CALLBACK;
            this._finCallback = callback;
        }

        @Override
        protected void compute() {
            if (this._handlers.size() == 1) {
                try {
                    this._handlers.get(0).handle(this._event);
                } catch (Exception ex) {
                    EventBus.this._logger.error(ex);
                }
                if (this._waitType == WaitType.CALLBACK) {
                    try {
                        this._finCallback.callback(this._event);
                    } catch (Exception ex) {
                        EventBus.this._logger.error(ex);
                    }
                }
                return;
            }

            if (this._waitType == WaitType.NO_WAIT) {
                Looper.on(this._handlers)
                        .map(handler -> new HandleEventAction(handler, this._event))
                        .foreach(action -> EventBus.this._fjPoll.submit(action));
            } else {
                List<ForkJoinTask<Void>> tasks = Looper.on(this._handlers)
                        .map(handler -> new HandleEventAction(handler, this._event))
                        .map(action -> EventBus.this._fjPoll.submit(action))
                        .toList();
                Looper.on(tasks).foreach(ForkJoinTask::join);
                if (this._waitType == WaitType.CALLBACK) {
                    this._finCallback.callback(this._event);
                }
            }
        }
    }
}
