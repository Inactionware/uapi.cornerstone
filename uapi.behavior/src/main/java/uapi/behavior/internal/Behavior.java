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
import uapi.common.ArgumentChecker;
import uapi.common.Builder;
import uapi.common.Functionals;
import uapi.common.Repository;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A Behavior represent a serial actions to process input data to output data
 */
public class Behavior<I, O>
        extends Builder<IBehavior<I, O>>
        implements IBehavior<I, O>, IBehaviorBuilder {

    private ActionIdentify _actionId;
    private Class<I> _iType;
    private Class<O> _oType;

    private final Repository<String, IAction<?, ?>> _actionRepo;
    private final ActionHolder _entryAction;
    private final Navigator _navigator;

    private Functionals.Evaluator _lastEvaluator;

    Behavior(Repository<String, IAction<?, ?>> actionRepository, Class inputType) {
        ArgumentChecker.required(actionRepository, "responsible");
        ArgumentChecker.required(inputType, "inputType");
        this._actionRepo = actionRepository;
        EndpointAction starting = new EndpointAction(inputType);
        this._entryAction = new ActionHolder(starting);

        this._navigator = new Navigator(this._entryAction);
    }

    // ----------------------------------------------------
    // Methods implement from IIdentifiable interface
    // ----------------------------------------------------

    @Override
    public String getId() {
        ensureBuilt();
        return this._actionId.getId();
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
    public O process(I input, IExecutionContext context) {
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

    @Override
    public IBehaviorBuilder id(ActionIdentify actionId) {
        ensureNotBuilt();
        ArgumentChecker.required(actionId, "actionId");
        this._actionId = actionId;
        return this;
    }

    @Override
    public IBehaviorBuilder traceable(boolean traceable) {
        ensureNotBuilt();
        // Todo: set trace
        return null;
    }

    @Override
    public IBehaviorBuilder when(Functionals.Evaluator evaluator) {
        ensureNotBuilt();
        ArgumentChecker.required(evaluator, "evaluator");
        if (this._lastEvaluator != null) {
            throw new GeneralException("The evaluator is set");
        }
        this._lastEvaluator = evaluator;
        return this;
    }

    @Override
    public IBehaviorBuilder then(ActionIdentify id) {
        return then(id, null);
    }

    @Override
    public IBehaviorBuilder then(ActionIdentify id, String label) {
        ensureNotBuilt();
        ArgumentChecker.required(id, "id");
        IAction<?, ?> action = (IAction<?, ?>) this._actionRepo.get(id.getId());
        if (action == null) {
            throw new GeneralException("There is no action named - {}", id.getId());
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
    protected void validate() throws InvalidArgumentException {
        if (this._lastEvaluator != null) {
            throw new GeneralException("The evaluator is set but it does not used");
        }
        // Check all leaf action's output type, they must be a same type
        List<ActionHolder> leafActions = Looper.on(this._navigator._actions)
                .filter(ActionHolder::hasNext)
                .toList();
        Class outputType = leafActions.get(0).action().outputType();
        if (outputType == null) {
            throw new GeneralException("The behavior builder has no action defined");
        }
        if (leafActions.size() > 1) {
            Looper.on(leafActions).foreachWithIndex((idx, action) -> {
                if (idx == 0) {
                    return;
                }
                IAction action1 = leafActions.get(idx - 1).action();
                IAction action2 = leafActions.get(idx).action();
                if (!action1.outputType().equals(action2.outputType())) {
                    throw new GeneralException(
                            "Incorrect output type [{} vs. {}] between action [{} vs. {}]",
                            action1, action2, action1.outputType(), action2.outputType());
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
        // Do nothing
    }

    @Override
    protected IBehavior<I, O> createInstance() {
        return this;
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
        public String getId() {
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
                throw new InvalidArgumentException("No action is labeled - {}", label);
            }
            this._current = matched;
            return Behavior.this;
        }

        private void newNextAction(
                final IAction action,
                final Functionals.Evaluator evaluator,
                final String label
        ) {
            this._current = new ActionHolder(action, evaluator);
            if (! ArgumentChecker.isEmpty(label)) {
                ActionHolder existingAction = this._labeledActions.get(label);
                if (existingAction != null) {
                    throw new InvalidArgumentException(
                            "The label [{}] has been bind to action [{}]",
                            label, existingAction.action().getId());
                }
                this._labeledActions.put(label, this._current);
            }
        }
    }
}
