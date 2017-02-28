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
import uapi.common.Builder;
import uapi.common.Functionals;
import uapi.common.Repository;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Behavior represent a serial actions to process input data to output data
 */
public class Behavior<I, O>
        extends Builder<IBehavior<I, O>>
        implements IBehavior<I, O>, IBehaviorBuilder {

    private ActionIdentify _actionId;
    private Class<I> _iType;
    private Class<O> _oType;
    private boolean _traceable;

    private final Responsible _responsible;
    private final Repository<ActionIdentify, IAction<?, ?>> _actionRepo;
    private final ActionHolder _entryAction;
    private final Navigator _navigator;
    private final AtomicInteger _sequence;

    private Functionals.Evaluator _lastEvaluator;

    Behavior(
            final Responsible responsible,
            final Repository<ActionIdentify, IAction<?, ?>> actionRepository,
            final String name,
            final Class inputType
    ) {
        ArgumentChecker.required(responsible, "responsible");
        ArgumentChecker.required(actionRepository, "responsible");
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(inputType, "inputType");
        this._responsible = responsible;
        this._actionRepo = actionRepository;
        this._actionId = new ActionIdentify(name, ActionType.BEHAVIOR);
        EndpointAction starting = new EndpointAction(inputType);
        this._entryAction = new ActionHolder(starting);

        this._navigator = new Navigator(this._entryAction);
        this._sequence = new AtomicInteger(0);
    }

    // ----------------------------------------------------
    // Methods implement from IIdentifiable interface
    // ----------------------------------------------------

    @Override
    public ActionIdentify getId() {
        ensureBuilt();
        return this._actionId;
    }

    // ----------------------------------------------------
    // Methods implement from IAction interface
    // ----------------------------------------------------

    @Override
    public Class<I> inputType() {
        ensureBuilt();
        return this._iType;
    }

    @Override
    public Class<O> outputType() {
        ensureBuilt();
        return this._oType;
    }

    @Override
    public O process(final I input, final IExecutionContext context) {
        ensureBuilt();
        ActionHolder current = this._entryAction;
        Object data = input;
        while (current != null) {
            data = current.action().process(input, context);
        }
        return (O) data;
    }

    // ----------------------------------------------------
    // Methods implement from IBehaviorBuilder interface
    // ----------------------------------------------------

//    @Override
//    public IBehaviorBuilder name(final String name) {
//        ensureNotBuilt();
//        ArgumentChecker.required(name, "name");
//        this._actionId = new ActionIdentify(name, ActionType.BEHAVIOR);
//        return this;
//    }

    @Override
    public IBehaviorBuilder traceable(boolean traceable) {
        ensureNotBuilt();
        this._traceable = traceable;
        return this;
    }

    @Override
    public IBehaviorBuilder when(final Functionals.Evaluator evaluator) throws BehaviorException {
        ensureNotBuilt();
        ArgumentChecker.required(evaluator, "evaluator");
        if (this._lastEvaluator != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.EVALUATOR_IS_SET)
                    .variables(new BehaviorErrors.EvaluatorIsSet()
                            .actionId(this._navigator._current.action().getId()))
                    .build();
        }
        this._lastEvaluator = evaluator;
        return this;
    }

    @Override
    public IBehaviorBuilder then(final ActionIdentify id) throws BehaviorException {
        ensureNotBuilt();
        return then(id, null);
    }

    @Override
    public IBehaviorBuilder then(final ActionIdentify id, final String label) throws BehaviorException {
        ensureNotBuilt();
        ArgumentChecker.required(id, "id");
        IAction<?, ?> action = this._actionRepo.get(id);
        if (action == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_NOT_FOUND)
                    .variables(new BehaviorErrors.ActionNotFound()
                            .actionId(id))
                    .build();
        }
        this._navigator.newNextAction(action, this._lastEvaluator, label);
        this._lastEvaluator = null;
        return this;
    }

    @Override
    public INavigator navigator() {
        ensureNotBuilt();
        return this._navigator;
    }

    // ----------------------------------------------------
    // Methods extend from Builder class
    // ----------------------------------------------------

    @Override
    protected void validate() throws BehaviorException {
        ensureNotBuilt();
        if (this._lastEvaluator != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.EVALUATOR_NOT_USED)
                    .build();
//            throw new GeneralException("The evaluator is set but it does not used");
        }
        // Check all leaf action's output type, they must be a same type
        List<ActionHolder> leafActions = Looper.on(this._navigator._actions)
                .filter(ActionHolder::hasNext)
                .toList();
        Class outputType = leafActions.get(0).action().outputType();
        if (outputType == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.NO_ACTION_IN_BEHAVIOR)
                    .variables(new BehaviorErrors.NoActionInBehavior()
                            .behaviorId(this.getId()))
                    .build();
//            throw new GeneralException("The behavior builder has no action defined - {}", this.getId());
        }
        if (leafActions.size() > 1) {
            Looper.on(leafActions).foreachWithIndex((idx, action) -> {
                if (idx == 0) {
                    return;
                }
                IAction action1 = leafActions.get(idx - 1).action();
                IAction action2 = leafActions.get(idx).action();
                if (!action1.outputType().equals(action2.outputType())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.ACTION_IO_MISMATCH)
                            .variables(new BehaviorErrors.ActionIOMismatch()
                                    .outputType(action1.outputType())
                                    .inputType(action2.inputType())
                                    .outputAction(action1.getId())
                                    .inputAction(action2.getId()))
                            .build();
                }
            });
        }
        // Make all leaf action's next to a terminal action
        IAction terminal = new EndpointAction(outputType);
        Looper.on(leafActions).foreach(aHolder -> aHolder.next(terminal));

        this._iType = this._entryAction.action().inputType();
        this._oType = outputType;
    }

    @Override
    protected void beforeCreateInstance() {
        // Do nothing
    }

    @Override
    protected void afterCreateInstance() {
        this._responsible.publish(this);
    }

    @Override
    protected IBehavior<I, O> createInstance() {
        return this;
    }

    // ----------------------------------------------------
    // Non-public Methods
    // ----------------------------------------------------
    Execution newExecution() {
        ensureBuilt();
        return new Execution(this, this._sequence.incrementAndGet());
    }

    ActionHolder entryAction() {
        ensureBuilt();
        return this._entryAction;
    }

    boolean traceable() {
        return this._traceable;
    }

    private final class EndpointAction implements IAction {

        private final Class<?> _intputType;
        private final Class<?> _outputType;

        private EndpointAction(final Class inputType) {
            ArgumentChecker.required(inputType, "inputType");
            this._intputType = inputType;
            this._outputType = inputType;
        }

        @Override
        public ActionIdentify getId() {
            return null;
        }

        @Override
        public Class inputType() {
            return this._intputType;
        }

        @Override
        public Class outputType() {
            return this.outputType();
        }

        @Override
        public Object process(Object input, IExecutionContext context) {
            return input;
        }
    }

    private final class Navigator implements INavigator {

        private ActionHolder _current;
        private final ActionHolder _starting;
        private final Map<String, ActionHolder> _labeledActions;
        private final List<ActionHolder> _actions;

        private Navigator(final ActionHolder starting) {
            this._starting = starting;
            this._current = starting;
            this._labeledActions = new HashMap<>();
            this._actions = new LinkedList<>();
        }

        @Override
        public IBehaviorBuilder moveToStarting() {
            this._current = this._starting;
            return Behavior.this;
        }

        @Override
        public IBehaviorBuilder moveTo(final String label) {
            ArgumentChecker.required(label, "label");
            ActionHolder matched = this._labeledActions.get(label);
            if (matched == null) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.NO_ACTION_WITH_LABEL)
                        .variables(new BehaviorErrors.NoActionWithLabel()
                                .label(label))
                        .build();
            }
            this._current = matched;
            return Behavior.this;
        }

        private void newNextAction(
                final IAction action,
                final Functionals.Evaluator evaluator,
                final String label
        ) throws BehaviorException {
            this._current = new ActionHolder(action, evaluator);
            if (! ArgumentChecker.isEmpty(label)) {
                ActionHolder existingAction = this._labeledActions.get(label);
                if (existingAction != null) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.ACTION_LABEL_IS_BIND)
                            .variables(new BehaviorErrors.ActionLabelIsBind()
                                    .label(label)
                                    .actionId(existingAction.action().getId()))
                            .build();
                }
                this._labeledActions.put(label, this._current);
            }
        }
    }
}
