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
import uapi.common.ArgumentChecker;
import uapi.common.IAttributed;
import uapi.config.IntervalTime;
import uapi.config.annotation.Config;
import uapi.config.internal.IntervalTimeParser;
import uapi.event.*;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Event bus implementation
 */
//@ThreadSafe
@Service(IEventBus.class)
@Tag("Event")
public class EventBus implements IEventBus {

    private static final IntervalTime DEFAULT_AWAIT_TIME = IntervalTime.parse("10s");

    @Config(path="event.await-time", parser=IntervalTimeParser.class, optional=true)
    IntervalTime _awaitTime;

    @Inject
    @Optional
    protected List<IEventHandler> _eventHandlers = new CopyOnWriteArrayList<>();

    @Inject
    protected ILogger _logger;

    private ForkJoinPool _fjPoll = new ForkJoinPool();

    @Init
    protected void init() {
        if (this._awaitTime == null) {
            this._awaitTime = DEFAULT_AWAIT_TIME;
        }
    }

    @Override
    public void fire(
            final String topic
    ) throws NoEventHandlerException {
        this.fire(new PlainEvent(topic));
    }

    @Override
    public void fire(
            final String topic,
            boolean syncable
    ) throws NoEventHandlerException {
        this.fire(new PlainEvent(topic), syncable);
    }

    @Override
    public void fire(
            final IEvent event
    ) throws NoEventHandlerException {
        fire(event, false);
    }

    @Override
    public void fire(
            final IEvent event,
            final boolean syncable
    ) throws NoEventHandlerException {
        ArgumentChecker.required(event, "event");

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
        if (handlers.size() == 0) {
            throw new NoEventHandlerException(topic);
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
    public void register(IEventHandler eventHandler) {
        ArgumentChecker.required(eventHandler, "eventHandler");
        this._eventHandlers.add(eventHandler);
    }

    public void destroy() throws InterruptedException {
        this._fjPoll.shutdown();
        this._fjPoll.awaitTermination(this._awaitTime.seconds(), TimeUnit.SECONDS);
    }

    private class HandleEventAction extends RecursiveAction {

        private final IEvent _event;
        private final List<IEventHandler> _handlers;
        private final boolean _blocked;

        private HandleEventAction(IEventHandler handler, IEvent event) {
            this._event = event;
            this._handlers = new LinkedList<>();
            this._handlers.add(handler);
            this._blocked = false;
        }

        private HandleEventAction(List<IEventHandler> handlers, IEvent event, boolean blocked) {
            this._event = event;
            this._handlers = handlers;
            this._blocked = blocked;
        }

        @Override
        protected void compute() {
            if (this._handlers.size() == 1) {
                try {
                    this._handlers.get(0).handle(this._event);
                } catch (Exception ex) {
                    EventBus.this._logger.error(ex);
                }
                return;
            }

            List<ForkJoinTask<Void>> tasks = Looper.on(this._handlers)
                    .map(handler -> new HandleEventAction(handler, this._event))
                    .map(action -> EventBus.this._fjPoll.submit(action))
                    .toList();
            if (this._blocked) {
                Looper.on(tasks).foreach(ForkJoinTask::join);
            }
        }
    }
}
