/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.Type;
import uapi.behavior.*;
import uapi.common.*;
import uapi.common.Functionals;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Behavior represent a serial actions to process input data to output data
 */
public class Behavior
        extends Builder<IBehavior>
        implements IBehavior, IBehaviorBuilder {

    private static final String ACTION_LABEL_PREFIX = "__";
    private static final String LABEL_HEAD_ACTION   = ACTION_LABEL_PREFIX + "HEAD";
    private static final String LABEL_TAIL_ACTION   = ACTION_LABEL_PREFIX + "TAIL";

    private final ActionIdentify _actionId;
    private ActionInputMeta[] _iMetas;
    private ActionOutputMeta[] _oMetas;
    private boolean _traceable;

    private final Responsible _responsible;
//    private final Repository<ActionIdentify, IAction> _actionRepo;
    private final ActionRepository _actionRepo;
    private final ActionHolder _headAction;
    private final Navigator _navigator;
    private final IWired _wired;
    private final AtomicInteger _sequence;

    private Functionals.Evaluator _lastEvaluator;

    private uapi.behavior.Functionals.BehaviorSuccessAction _successAction;
    private uapi.behavior.Functionals.BehaviorFailureAction _failureAction;

    Behavior(
            final Responsible responsible,
//            final Repository<ActionIdentify, IAction> actionRepository,
            final ActionRepository actionRepository,
            final String name,
            final ActionInputMeta[] inputMetas
    ) {
        ArgumentChecker.required(responsible, "responsible");
        ArgumentChecker.required(actionRepository, "responsible");
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(inputMetas, "inputMetas");
        this._responsible = responsible;
        this._actionRepo = actionRepository;
        this._actionId = new ActionIdentify(name, ActionType.BEHAVIOR);
        HeadAction head = new HeadAction(inputMetas);
        this._iMetas = inputMetas;
        this._headAction = new ActionHolder(head, LABEL_HEAD_ACTION, this);

        this._navigator = new Navigator(this._headAction);
        this._wired = new Wired();
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
    public ActionInputMeta[] inputMetas() {
        ensureBuilt();
        return this._iMetas;
    }

    @Override
    public ActionOutputMeta[] outputMetas() {
        ensureBuilt();
        return this._oMetas;
    }

    @Override
    public void process(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context
    ) {
        ensureBuilt();
        Execution execution = newExecution();
        execution.execute(inputs, outputs, (ExecutionContext) context);
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
            final String label,
            final Object... inputs
    ) throws BehaviorException {
        ensureNotBuilt();
        ArgumentChecker.required(id, "id");
        var action = this._actionRepo.get(id);
        if (action == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_NOT_FOUND)
                    .variables(new BehaviorErrors.ActionNotFound()
                            .actionId(id))
                    .build();
        }
        this._navigator.newNextAction(action, this._lastEvaluator, label, inputs);
        this._lastEvaluator = null;
        return this;
    }

    @Override
    public IBehaviorBuilder then(
            final Class<?> actionType
    ) throws BehaviorException {
        return then(ActionIdentify.toActionId(actionType));
    }

    @Override
    public IBehaviorBuilder then(
            final Class<?> actionType,
            final String label,
            final Object... inputs
    ) throws BehaviorException {
        return then(ActionIdentify.toActionId(actionType), label, inputs);
    }

    @Override
    public IBehaviorBuilder call(
            final uapi.behavior.Functionals.AnonymousCall call
    ) {
        return call(call, null);
    }

    @Override
    public IBehaviorBuilder call(
            final uapi.behavior.Functionals.AnonymousCall call,
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
    public IBehaviorBuilder attributes(
            final Map<Object, Object> attrs
    ) throws BehaviorException {
        ensureNotBuilt();
        ArgumentChecker.required(attrs, "attrs");

        return null;
    }

    @Override
    public IBehaviorBuilder inputs(Object... inputs) throws BehaviorException {
        return null;
    }

    @Override
    public IBehaviorBuilder onSuccess(
            final uapi.behavior.Functionals.BehaviorSuccessAction action
    ) {
        ensureNotBuilt();
        ArgumentChecker.required(action, "action");
        if (this._successAction != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.SUCCESS_ACTION_EXISTS)
                    .variables(new BehaviorErrors.SuccessActionExists()
                            .behaviorId(this._actionId))
                    .build();
        }
        this._successAction = action;
        return this;
    }

    @Override
    public IBehaviorBuilder onFailure(
            final uapi.behavior.Functionals.BehaviorFailureAction action
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
        this._failureAction = action;
        return this;
    }

    @Override
    public INavigator navigator() {
        ensureNotBuilt();
        return this._navigator;
    }

    @Override
    public IWired wired() {
        ensureNotBuilt();
        return this._wired;
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
        var leafActions = Looper.on(this._navigator._actions)
                .filter(actionHolder -> ! actionHolder.hasNext())
                .toList();
        if (leafActions.size() == 0) {
            // Impossible happen
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.EMPTY_BEHAVIOR)
                    .variables(new BehaviorErrors.EmptyBehavior()
                            .behaviorId(this._actionId))
                    .build();
        }
        // Make sure all leaf action must be has same output type
        if (leafActions.size() > 1) {
            Looper.on(leafActions).foreachWithIndex((idx, action) -> {
                if (idx == 0) {
                    return;
                }
                var leafAction1 = leafActions.get(idx - 1).action();
                var leafAction2 = leafActions.get(idx).action();
                var outMetas1 = leafAction1.outputMetas();
                var outMetas2 = leafAction2.outputMetas();
                if (outMetas1.length != outMetas2.length) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.INCONSISTENT_LEAF_ACTIONS)
                            .variables(new BehaviorErrors.InconsistentLeafActions()
                                    .leafAction1(leafAction1.getId())
                                    .leafAction2(leafAction2.getId())
                                    .leafAction1Output(outMetas1)
                                    .leafAction2Output(outMetas2))
                            .build();
                }
                // ensure all action output must have same type
                if (outMetas1.length > 0) {
                    int unmatchedPos = Looper.on(Range.from(0).to(outMetas1.length - 1).iterator())
                            .filter(pos -> ! outMetas1[pos].type().equals(outMetas2[pos].type()))
                            .first(-1);
                    if (unmatchedPos != -1) {
                        throw BehaviorException.builder()
                                .errorCode(BehaviorErrors.INCONSISTENT_LEAF_ACTIONS)
                                .variables(new BehaviorErrors.InconsistentLeafActions()
                                        .leafAction1(leafAction1.getId())
                                        .leafAction2(leafAction2.getId())
                                        .leafAction1Output(outMetas1)
                                        .leafAction2Output(outMetas2))
                                .build();
                    }
                }
            });
        }

        // Make all leaf action's next to a exit action
//        IAction exit = new EndpointAction(EndpointType.EXIT, leafActions.get(0).outputMetas());
//        Looper.on(leafActions).foreach(aHolder -> aHolder.next(new ActionHolder(exit, this)));

        this._oMetas = leafActions.get(0).outputMetas();
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
    protected IBehavior createInstance() {
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

    ActionHolder headAction() {
        ensureBuilt();
        return this._headAction;
    }

    int actionSize() {
        return Looper.on(this._navigator._actions)
                .map(ActionHolder::action)
                .filter(action -> ! (action.getClass().isAssignableFrom(HeadAction.class)))
                .count();
    }

    private final class HeadAction implements IAction {

        private final ActionIdentify _id;
        private final ActionInputMeta[] _inputMetas = new ActionInputMeta[0];
        private final ActionOutputMeta[] _outputMetas;

        private HeadAction(
                final ActionInputMeta[] behaviorInputMetas
        ) {
            ArgumentChecker.required(behaviorInputMetas, "behaviorInputMetas");

            this._id = ActionIdentify.toActionId(HeadAction.class);
            this._outputMetas = new ActionOutputMeta[behaviorInputMetas.length];
            Looper.on(behaviorInputMetas).foreachWithIndex((idx, inMeta) -> {
                this._outputMetas[idx] = new ActionOutputMeta(inMeta.type());
            });
        }

        @Override
        public ActionIdentify getId() {
            return this._id;
        }

        @Override
        public ActionInputMeta[] inputMetas() {
            return this._inputMetas;
        }

        @Override
        public ActionOutputMeta[] outputMetas() {
            return this._outputMetas;
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public void process(Object[] inputs, ActionOutput[] outputs, IExecutionContext context) {
            var behaviorInputs = context.behaviorInputs();
            Looper.on(behaviorInputs).foreachWithIndex((idx, input) -> outputs[idx].set(input));
        }
    }

//    private class TailAction implements IAction {
//
//        private final ActionIdentify _id;
//        private final ActionInputMeta[] _inMetas;
//        private final ActionOutputMeta[] _outMetas;
//
//        private TailAction(final ActionOutputMeta[] leafActionOutputMetas) {
//            ArgumentChecker.required(leafActionOutputMetas, "leafActionOutputMetas");
//
//            this._id = ActionIdentify.toActionId(TailAction.class);
//            this._inMetas = new ActionInputMeta[leafActionOutputMetas.length];
//            this._outMetas = new ActionOutputMeta[leafActionOutputMetas.length];
//            Looper.on(leafActionOutputMetas).foreachWithIndex((idx, outMeta) -> {
//                this._inMetas[idx] = new ActionInputMeta(leafActionOutputMetas[idx].type());
//                this._outMetas[idx] = new ActionOutputMeta(leafActionOutputMetas[idx].type());
//            });
//        }
//
//        @Override
//        public ActionIdentify getId() {
//            return this._id;
//        }
//
//        @Override
//        public ActionInputMeta[] inputMetas() {
//            return this._inMetas;
//        }
//
//        @Override
//        public ActionOutputMeta[] outputMetas() {
//            return this._outMetas;
//        }
//
//        @Override
//        public boolean isAnonymous() {
//            return false;
//        }
//
//        @Override
//        public void process(Object[] inputs, ActionOutput[] outputs, IExecutionContext context) {
//            Looper.on(inputs).foreachWithIndex((idx, input) -> outputs[idx].set(input));
//        }
//    }

    private final class Navigator implements INavigator {

        private static final int MAX_TRY_ID_COUNT       = 10;

        private ActionHolder _current;
        private ActionHolder _head;
        private final Map<String, ActionHolder> _labeledActions;
        private final List<ActionHolder> _actions;

        private Navigator(final ActionHolder head) {
            this._head = head;
            this._current = head;
            this._labeledActions = new HashMap<>();
            this._actions = new LinkedList<>();
        }

        @Override
        public IBehaviorBuilder moveToHead() {
            this._current = this._head;
            return Behavior.this;
        }

        @Override
        public IBehaviorBuilder moveTo(final String label) {
            ArgumentChecker.required(label, "label");
            var matched = this._labeledActions.get(label);
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
                final String label,
                final Object... inputs
        ) throws BehaviorException {
            var actionLabel = label;
            if (StringHelper.isNullOrEmpty(label)) {
                // Generate action label if no label is specified
                var actionId = action.getId().getName();
                actionLabel = actionId;
                int idx = 1;
                while (this._labeledActions.containsKey(actionLabel)) {
                    if (idx > MAX_TRY_ID_COUNT) {
                        throw BehaviorException.builder()
                                .errorCode(BehaviorErrors.GENERATE_ACTION_LABEL_OVER_MAX)
                                .variables(new BehaviorErrors.GenerateActionLabelOverMax()
                                        .actionId(action.getId())
                                        .maxCount(MAX_TRY_ID_COUNT))
                                .build();
                    }
                    actionLabel = StringHelper.makeString("{}-{}", actionId, idx);
                    idx++;
                }
            } else {
                // Customized action label
                if (this._labeledActions.containsKey(label)) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.DUPLICATED_ACTION_LABEL)
                            .variables(new BehaviorErrors.DuplicatedActionLabel()
                                    .label(label)
                                    .behaviorId(Behavior.this._actionId))
                            .build();
                }
            }

            // Auto wire to last action outputs if new action is not specified inputs
            var actionInputs = inputs;
            if (actionInputs.length == 0 && action.inputMetas().length > 0 && this._current.outputMetas().length > 0) {
                var inMetas = action.inputMetas();
                var outMetas = this._current.outputMetas();
                if (action.inputMetas().length != this._current.outputMetas().length) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.AUTO_WIRE_IO_NOT_MATCH)
                            .variables(new BehaviorErrors.AutoWireIONotMatch()
                                    .fromActionId(this._current.action().getId())
                                    .toActionId(action.getId())
                                    .fromActionOutputMetas(outMetas)
                                    .toActionInputMetas(inMetas))
                            .build();
                }
                Looper.on(inMetas).foreachWithIndex((idx, inMeta) -> {
                    if (! Type.isAssignable(outMetas[idx].type(), inMeta.type())) {
                        throw BehaviorException.builder()
                                .errorCode(BehaviorErrors.AUTO_WIRE_IO_NOT_MATCH)
                                .variables(new BehaviorErrors.AutoWireIONotMatch()
                                        .fromActionId(this._current.action().getId())
                                        .toActionId(action.getId())
                                        .fromActionOutputMetas(outMetas)
                                        .toActionInputMetas(inMetas))
                                .build();
                    }
                });
                var idx = Numeric.mutableInteger(0);
                actionInputs = Looper.on(outMetas)
                        .map(outMeta -> Behavior.this.wired().toOutput(this._current.label(), idx.getAndIncrease()))
                        .toArray();
            }

            // create new action holder
            if (action instanceof IIntercepted) {
                this._current = new InterceptedActionHolder(
                        Behavior.this._actionRepo, action, actionLabel, this._current, Behavior.this, evaluator, actionInputs);
            } else {
                this._current = new ActionHolder(
                        action, actionLabel, this._current, Behavior.this, evaluator, actionInputs);
            }
            this._actions.add(this._current);
            this._labeledActions.put(actionLabel, this._current);
        }
    }

    private final class Wired implements IWired {

        @Override
        public IOutputReference toOutput(String actionLabel, String outputName) {
            return new NamedOutput(actionLabel, outputName);
        }

        @Override
        public IOutputReference toOutput(String actionLabel, int actionIndex) {
            return new IndexedOutput(actionLabel, actionIndex);
        }
    }

    public class NamedOutput implements IOutputReference {

        private final String _label;
        private final String _name;

        private NamedOutput(
                final String label,
                final String name
        ) {
            ArgumentChecker.required(label, "label");
            ArgumentChecker.required(name, "name");
            var currentAction = Behavior.this._navigator._current;
            var found = false;
            while (currentAction != null) {
                if (currentAction.label().equals(label)) {
                    var meta = Looper.on(currentAction.outputMetas())
                            .filter(outMeta -> outMeta.name().equals(name))
                            .first();
                    if (meta != null) {
                        found = true;
                        break;
                    }
                }
                currentAction = currentAction.previous();
            }
            if (! found) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.NAMED_OUTPUT_REF_NOT_FOUND)
                        .variables(new BehaviorErrors.NamedOutputRefNotFound()
                                .actionLabel(label)
                                .outputName(name)
                                .behaviorId(Behavior.this.getId()))
                        .build();
            }
            this._label = label;
            this._name = name;
        }

        @Override
        public String actionLabel() {
            return this._label;
        }

        String outputName() {
            return this._name;
        }

        @Override
        public String toString() {
            return StringHelper.makeString("{}@{}", this._name, this._label);
        }
    }

    public class IndexedOutput implements IOutputReference {

        private final String _label;
        private final int _idx;

        private IndexedOutput(
                final String label,
                final int index
        ) {
            ArgumentChecker.required(label, "label");
            ArgumentChecker.checkInt(index, "index", 0, Integer.MAX_VALUE);
            var currentAction = Behavior.this._navigator._current;
            var found = false;
            while (currentAction != null) {
                if (currentAction.label().equals(label)) {
                    if (index < currentAction.outputMetas().length) {
                        found = true;
                    }
                    break;
                }
                currentAction = currentAction.previous();
            }
            if (! found) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.INDEXED_OUTPUT_REF_INVALID)
                        .variables(new BehaviorErrors.IndexedOutputRefInvalid()
                                .actionLabel(label)
                                .index(index)
                                .behaviorId(Behavior.this.getId()))
                        .build();
            }
            this._label = label;
            this._idx = index;
        }

        @Override
        public String actionLabel() {
            return this._label;
        }

        int outputIndex() {
            return this._idx;
        }

        @Override
        public String toString() {
            return StringHelper.makeString("{}@{}", this._idx, this._label);
        }
    }
}
