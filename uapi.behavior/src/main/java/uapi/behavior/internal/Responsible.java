/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.InvalidArgumentException;
import uapi.behavior.*;
import uapi.common.*;
import uapi.event.IAttributedEventHandler;
import uapi.event.IEventBus;
import uapi.event.IEventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * A responsible is used to fire behavior event and define specific behaviors based on behavior event
 */
public class Responsible implements IResponsible {

    private final String _name;
    private final Repository<ActionIdentify, IAction<?, ?>> _actionRepo;
    private final IEventBus _eventBus;

    private final Map<ActionIdentify, BehaviorHolder> _behaviors;

    private BehaviorExecutingEventHandler _behaviorExecutingHandler;
    private BehaviorFinishedEventHandler _behaviorFinishedHandler;
    private boolean _traceEventHandlerRegistered = false;

    Responsible(
            final String name,
            final IEventBus eventBus,
            final Repository<ActionIdentify, IAction<?, ?>> actionRepository
    ) {
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(eventBus, "eventBus");
        ArgumentChecker.required(actionRepository, "actionRepository");
        this._name = name;
        this._eventBus = eventBus;
        this._actionRepo = actionRepository;
        this._behaviors = new HashMap<>();
    }

    @Override
    public String name() {
        return this._name;
    }

    @Override
    public IBehaviorBuilder newBehavior(
            final String name,
            final String topic
    ) throws BehaviorException {
        ArgumentChecker.required(topic, "topic");
        Behavior behavior = new Behavior(this, this._actionRepo, name, BehaviorEvent.class);
        ActionIdentify behaviorId = behavior.getId();
        if (this._behaviors.containsKey(behaviorId)) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.BEHAVIOR_ID_IS_USED)
                    .variables(new BehaviorErrors.BehaviorIdIsUsed()
                            .behaviorId(behaviorId.getId()).get())
                    .build();
        }
        this._behaviors.put(behavior.getId(), new BehaviorHolder(behavior, topic));
        return behavior;
    }

    @Override
    public IBehaviorBuilder newBehavior(
            final String name,
            final Class<?> type
    ) throws BehaviorException {
        Behavior behavior = new Behavior(this, this._actionRepo, name, type);
        ActionIdentify behaviorId = behavior.getId();
        if (this._behaviors.containsKey(behaviorId)) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.BEHAVIOR_ID_IS_USED)
                    .variables(new BehaviorErrors.BehaviorIdIsUsed()
                            .behaviorId(behaviorId.getId()).get())
                    .build();
        }
        this._behaviors.put(behavior.getId(), new BehaviorHolder(behavior));
        return behavior;
    }

    @Override
    public void on(BehaviorExecutingEventHandler handler) {
        ArgumentChecker.required(handler, "handler");
        this._behaviorExecutingHandler = handler;
        registerEventHandler();
    }

    @Override
    public void on(BehaviorFinishedEventHandler handler) {
        ArgumentChecker.required(handler, "handler");
        this._behaviorFinishedHandler = handler;
        registerEventHandler();
    }

    void publish(final Behavior behavior) {
        ArgumentChecker.required(behavior, "behavior");
        ActionIdentify behaviorId = behavior.getId();
        BehaviorHolder behaviorHolder = this._behaviors.get(behaviorId);
        if (behaviorHolder == null) {
            throw new InvalidArgumentException(
                    "Can't publish behavior - {} in - {}, reason: not found", behaviorId, this._name);
        }
        if (behaviorHolder.isPublished()) {
            throw new GeneralException("The behavior - {} in {} is published", behaviorId, this._name);
        }
        String topic = behaviorHolder.topic();
        if (ArgumentChecker.isEmpty(topic)) {
            this._actionRepo.put(behavior);
        } else {
            BehaviorEventHandler handler = new BehaviorEventHandler(topic, behavior);
            this._eventBus.register(handler);
        }
        behaviorHolder.setPublished();
    }

    private void registerEventHandler() {
        if (this._traceEventHandlerRegistered) {
            return;
        }
        this._eventBus.register(new BehaviorTraceEventHandler());
        this._traceEventHandlerRegistered = true;
    }

    private final class BehaviorEventHandler implements IAttributedEventHandler<BehaviorEvent> {

        private final String _topic;
        private final Behavior<?, ?> _behavior;

        private BehaviorEventHandler(
                final String topic,
                final Behavior<?, ?> behavior
        ) {
            this._topic = topic;
            this._behavior = behavior;
        }

        @Override
        public String topic() {
            return this._topic;
        }

        @Override
        public Map<Object, Object> getAttributes() {
            return null;
        }

        @Override
        public void handle(BehaviorEvent event) {
            Execution exec = this._behavior.newExecution();
            ExecutionContext exeCtx = new ExecutionContext(Responsible.this._eventBus);
            // Ignore the output data
            exec.execute(event, exeCtx);
        }
    }

    private final class BehaviorTraceEventHandler implements IEventHandler<IBehaviorTraceEvent> {

        @Override
        public String topic() {
            return IBehaviorTraceEvent.TOPIC;
        }

        @Override
        public void handle(IBehaviorTraceEvent event) {
            if (event instanceof BehaviorExecutingEvent) {
                handleExecutingEvent((BehaviorExecutingEvent) event);
            } else if (event instanceof BehaviorFinishedEvent) {
                handleFinishedEvent((BehaviorFinishedEvent) event);
            } else {
                throw new GeneralException("Unsupported event type - {}", event.getClass().getName());
            }
        }

        private void handleExecutingEvent(BehaviorExecutingEvent event) {
            if (Responsible.this._behaviorExecutingHandler != null) {
                Responsible.this._behaviorExecutingHandler.accept(event);
            }
        }

        private void handleFinishedEvent(BehaviorFinishedEvent event) {
            if (Responsible.this._behaviorFinishedHandler != null) {
                Responsible.this._behaviorFinishedHandler.accept(event);
            }
        }
    }

    private static final class BehaviorHolder extends Multivariate {

        private static final int IDX_BEHAVIOR   = 0;
        private static final int IDX_TOPIC      = 1;
        private static final int IDX_PUBLISHED  = 2;

        private BehaviorHolder(final Behavior behavior) {
            this(behavior, null, false);
        }

        private BehaviorHolder(final Behavior behavior, final String topic) {
            this(behavior, topic, false);
        }

        private BehaviorHolder(final Behavior behavior, final String topic, final boolean published) {
            super(3);
            ArgumentChecker.required(topic, "topic");
            put(IDX_BEHAVIOR, behavior);
            put(IDX_TOPIC, topic);
            put(IDX_PUBLISHED, published);
        }

        private Behavior behavior() {
            return get(IDX_BEHAVIOR);
        }

        public String topic() {
            return get(IDX_TOPIC);
        }

        public boolean isPublished() {
            return get(IDX_PUBLISHED);
        }

        public void setPublished() {
            put(IDX_PUBLISHED, true);
        }
    }
}
