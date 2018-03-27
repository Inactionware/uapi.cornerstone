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
import uapi.event.IEventFinishCallback;
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

    private final ActionIdentify _actionId;
    private Class<I> _iType;
    private Class<O> _oType;
    private boolean _traceable;

    private final Responsible _responsible;
    private final Repository<ActionIdentify, IAction<?, ?>> _actionRepo;
    private final ActionHolder _entranceAction;
    private final Navigator _navigator;
    private final AtomicInteger _sequence;

    private Functionals.Evaluator _lastEvaluator;

//    private IAnonymousAction<Object, BehaviorEvent> _successAction;
//    private IAnonymousAction<Exception, BehaviorEvent> _failureAction;

    private IAction<BehaviorSuccess, BehaviorEvent> _successAction;
    private IAction<BehaviorFailure, BehaviorEvent> _failureAction;

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
        EndpointAction entrance = new EndpointAction(EndpointType.ENTRANCE, inputType);
        this._entranceAction = new ActionHolder(entrance);

        this._navigator = new Navigator(this._entranceAction);
        this._sequence = new AtomicInteger(0);
    }

    // ----------------------------------------------------
    // Methods implement from IIdentifiable interface
    // ----------------------------------------------------

    @Override
    public ActionIdentify getId() {
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
    public O process(
            final I input,
            final IExecutionContext context
    ) {
        Execution execution = newExecution();
        return (O) execution.execute(input, (ExecutionContext) context);
    }

    // ----------------------------------------------------
    // Methods implement from IBehavior interface
    // ----------------------------------------------------
    @Override
    public boolean traceable() {
        return this._traceable;
    }

    // ----------------------------------------------------
    // Methods implement from IBehaviorBuilder interface
    // ----------------------------------------------------

    @Override
    public IBehaviorBuilder traceable(
            final boolean traceable
    ) {
        ensureNotBuilt();
        this._traceable = traceable;
        return this;
    }

    @Override
    public IBehaviorBuilder when(
            final Functionals.Evaluator evaluator
    ) throws BehaviorException {
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
    public IBehaviorBuilder then(
            final ActionIdentify id
    ) throws BehaviorException {
        return then(id, null);
    }

    @Override
    public IBehaviorBuilder then(
            final ActionIdentify id,
            final String label
    ) throws BehaviorException {
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
    public IBehaviorBuilder then(
            final IAnonymousAction<?, ?> action
    ) {
        return then(action, null);
    }

    @Override
    public IBehaviorBuilder then(
            final IAnonymousAction<?, ?> action,
            final String label
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(action, "action");
        AnonymousAction aAction = new AnonymousAction(action);
        this._navigator.newNextAction(aAction, this._lastEvaluator, label);
        this._lastEvaluator = null;
        return this;
    }

    @Override
    public IBehaviorBuilder call(
            final IAnonymousCall<?> call
    ) {
        return call(call, null);
    }

    @Override
    public IBehaviorBuilder call(
            final IAnonymousCall<?> call,
            final String label
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(call, "call");
        AnonymousCall aAction = new AnonymousCall(call);
        this._navigator.newNextAction(aAction, this._lastEvaluator, label);
        this._lastEvaluator = null;
        return this;
    }

    @Override
    public IBehaviorBuilder onSuccess(
            final IAnonymousAction<BehaviorSuccess, BehaviorEvent> action
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(action, "action");
        if (this._successAction != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.SUCCESS_ACTION_EXISTS)
                    .variables(new BehaviorErrors.FailureActionExists()
                            .behaviorId(this._actionId))
                    .build();
        }
        this._successAction = new AnonymousAction<>(action);
        return this;
    }

    @Override
    public IBehaviorBuilder onSuccess(
            final ActionIdentify actionId
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(actionId, "actionId");
        if (this._successAction != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.SUCCESS_ACTION_EXISTS)
                    .variables(new BehaviorErrors.FailureActionExists()
                            .behaviorId(this._actionId))
                    .build();
        }
        IAction<?, ?> action = this._actionRepo.get(actionId);
        if (action == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_NOT_FOUND)
                    .variables(new BehaviorErrors.ActionNotFound()
                            .actionId(actionId))
                    .build();
        }
        this._successAction = (IAction<BehaviorSuccess, BehaviorEvent>) action;
        return this;
    }

    @Override
    public IBehaviorBuilder onFailure(
            final IAnonymousAction<BehaviorFailure, BehaviorEvent> action
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(action, "action");
        if (this._failureAction != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.FAILURE_ACTION_EXISTS)
                    .variables(new BehaviorErrors.FailureActionExists()
                            .behaviorId(this._actionId))
                    .build();
        }
        this._failureAction = new AnonymousAction<>(action);
        return this;
    }

    @Override
    public IBehaviorBuilder onFailure(
            final ActionIdentify actionId
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(actionId, "actionId");
        if (this._failureAction != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.FAILURE_ACTION_EXISTS)
                    .variables(new BehaviorErrors.FailureActionExists()
                            .behaviorId(this._actionId))
                    .build();
        }
        IAction<?, ?> action = this._actionRepo.get(actionId);
        if (action == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_NOT_FOUND)
                    .variables(new BehaviorErrors.ActionNotFound()
                            .actionId(actionId))
                    .build();
        }
        this._failureAction = (IAction<BehaviorFailure, BehaviorEvent>) action;
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
        }
        // Check all leaf action's output type, they must be a same type
        List<ActionHolder> leafActions = Looper.on(this._navigator._actions)
                .filter(actionHolder -> ! actionHolder.hasNext())
                .toList();
        if (leafActions.size() == 0) {
            // Impossible happen
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.NO_ACTION_IN_BEHAVIOR)
                    .variables(new BehaviorErrors.NoActionInBehavior()
                            .behaviorId(this._actionId))
                    .build();
        }
        // Make sure all leaf action must be has same output type
        if (leafActions.size() > 1) {
            Looper.on(leafActions).foreachWithIndex((idx, action) -> {
                if (idx == 0) {
                    return;
                }
                IAction action1 = leafActions.get(idx - 1).action();
                IAction action2 = leafActions.get(idx).action();
                if (!action1.outputType().equals(action2.outputType())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.INCONSISTENT_LEAF_ACTIONS)
                            .variables(new BehaviorErrors.InconsistentLeafActions()
                                    .leafAction1(action1.getId())
                                    .leafAction2(action2.getId())
                                    .leafAction1Output(action1.outputType())
                                    .leafAction2Output(action2.outputType()))
                            .build();
                }
            });
        }

        // Make all leaf action's next to a exit action
        Class outputType = leafActions.get(0).action().outputType();
        IAction exit = new EndpointAction(EndpointType.EXIT, outputType);
        Looper.on(leafActions).foreach(aHolder -> aHolder.next(new ActionHolder(exit)));

        this._iType = this._entranceAction.action().inputType();
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
        return new Execution(
                this,
                this._sequence.incrementAndGet(),
                this._successAction,
                this._failureAction);
    }

    ActionHolder entranceAction() {
        ensureBuilt();
        return this._entranceAction;
    }

    private final class EndpointAction implements IAction {

        private final EndpointType _type;
        private final Class<?> _inputType;
        private final Class<?> _outputType;

        private EndpointAction(
                final EndpointType type,
                final Class inputType
        ) {
            ArgumentChecker.required(inputType, "inputType");
            this._inputType = inputType;
            this._outputType = inputType;
            this._type = type;
        }

        @Override
        public ActionIdentify getId() {
            return new ActionIdentify(this._type.name(), ActionType.ACTION);
        }

        @Override
        public Class inputType() {
            return this._inputType;
        }

        @Override
        public Class outputType() {
            return this._outputType;
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public Object process(Object input, IExecutionContext context) {
            return input;
        }
    }

    private enum EndpointType {
        ENTRANCE, EXIT
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
            ActionHolder newAction = new ActionHolder(action, evaluator);
            // Check new action input is matched to current action output
            // The check only on non-anonymous action
            if (! this._current.action().isAnonymous() && ! action.isAnonymous()) {
                if (! action.inputType().isAssignableFrom(this._current.action().outputType())) {
//                if (!this._current.action().outputType().equals(action.inputType())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.ACTION_IO_MISMATCH)
                            .variables(new BehaviorErrors.ActionIOMismatch()
                                    .outputAction(this._current.action().getId())
                                    .outputType(this._current.action().outputType())
                                    .inputAction(action.getId())
                                    .inputType(action.inputType()))
                            .build();
                }
            }
            // Check action label
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
                this._labeledActions.put(label, newAction);
            }

            this._current.next(newAction);
            this._current = newAction;
            this._actions.add(newAction);
        }
    }
}
