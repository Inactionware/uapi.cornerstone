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
import uapi.common.*;
import uapi.event.IAttributedEventHandler;
import uapi.event.IEventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean _traceEventHandlerRegistered;

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
        this._traceEventHandlerRegistered = new AtomicBoolean(false);
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
                            .behaviorId(behaviorId).get())
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
                            .behaviorId(behaviorId).get())
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

    void publish(final Behavior behavior) throws BehaviorException {
        ArgumentChecker.required(behavior, "behavior");
        ActionIdentify behaviorId = behavior.getId();
        BehaviorHolder behaviorHolder = this._behaviors.get(behaviorId);
        if (behaviorHolder == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.PUBLISH_UNREG_BEHAVIOR)
                    .variables(new BehaviorErrors.PublishUnregBehavior()
                            .behaviorId(behaviorId)
                            .responsibleName(this._name))
                    .build();
        }
        if (behaviorHolder.isPublished()) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.BEHAVIOR_IS_PUBLISHED)
                    .variables(new BehaviorErrors.BehaviorIsPublished()
                            .behaviorId(behaviorId)
                            .responsibleName(this._name))
                    .build();
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
        boolean registered = this._traceEventHandlerRegistered.getAndSet(true);
        if (! registered) {
            this._eventBus.register(new BehaviorTraceEventHandler());
        }
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
            exeCtx.put(IExecutionContext.KEY_RESP_NAME, Responsible.this._name, Scope.GLOBAL);
            // Ignore the output data
            exec.execute(event, exeCtx);
        }
    }

    private final class BehaviorTraceEventHandler implements IAttributedEventHandler<BehaviorTraceEvent> {

        private final Map<Object, Object> _attributes = new HashMap<>();

        private BehaviorTraceEventHandler() {
            this._attributes.put(BehaviorTraceEvent.KEY_RESP_NAME, Responsible.this._name);
        }

        @Override
        public String topic() {
            return BehaviorTraceEvent.TOPIC;
        }

        @Override
        public Map<Object, Object> getAttributes() {
            return this._attributes;
        }

        @Override
        public void handle(BehaviorTraceEvent event) {
            if (event instanceof BehaviorExecutingEvent) {
                handleExecutingEvent((BehaviorExecutingEvent) event);
            } else if (event instanceof BehaviorFinishedEvent) {
                handleFinishedEvent((BehaviorFinishedEvent) event);
            } else {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.UNSUPPORTED_BEHAVIOR_EVENT_TYPE)
                        .variables(new BehaviorErrors.UnsupportedBehaviorTraceEventType()
                                .eventType(event.getClass()))
                        .build();
            }
        }

        private void handleExecutingEvent(BehaviorExecutingEvent event) {
            if (Responsible.this._behaviorExecutingHandler != null) {
                BehaviorEvent bEvent = Responsible.this._behaviorExecutingHandler.accept(event);
                if (bEvent != null) {
                    Responsible.this._eventBus.fire(bEvent);
                }
            }
        }

        private void handleFinishedEvent(BehaviorFinishedEvent event) {
            if (Responsible.this._behaviorFinishedHandler != null) {
                BehaviorEvent bEvent = Responsible.this._behaviorFinishedHandler.accept(event);
                if (bEvent != null) {
                    Responsible.this._eventBus.fire(bEvent);
                }
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
