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

    private final ActionIdentify _actionId;
    private ActionInputMeta[] _iMetas;
    private ActionOutputMeta[] _oMetas;
    private boolean _traceable;

    private final Responsible _responsible;
    private final Repository<ActionIdentify, IAction> _actionRepo;
    private final ActionHolder _entranceAction;
    private final Navigator _navigator;
    private final AtomicInteger _sequence;

    private Functionals.Evaluator _lastEvaluator;

    private IBehaviorSuccessCall _successAction;
    private IBehaviorFailureCall _failureAction;

    Behavior(
            final Responsible responsible,
            final Repository<ActionIdentify, IAction> actionRepository,
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
        EndpointAction entrance = new EndpointAction(EndpointType.ENTRANCE, inputMetas);
        this._entranceAction = new ActionHolder(entrance, this);

        this._navigator = new Navigator(this._entranceAction);
        this._sequence = new AtomicInteger(0);
    }

    Behavior(
            final Responsible responsible,
            final Repository<ActionIdentify, IAction> actionRepository,
            final String name,
            final Class<?> inputType
    ) {
        this(responsible, actionRepository, name, new ActionInputMeta[] { new ActionInputMeta(inputType) });
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
        IAction action = this._actionRepo.get(id);
        if (action == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_NOT_FOUND)
                    .variables(new BehaviorErrors.ActionNotFound()
                            .actionId(id))
                    .build();
        }
//        if (addInterceptor(action, label)) {
//            this._navigator.newNextAction(action, this._lastEvaluator, null, inputs);
//        } else {
//            this._navigator.newNextAction(action, this._lastEvaluator, label, inputs);
//        }
        this._navigator.newNextAction(action, this._lastEvaluator, label, inputs);
        this._lastEvaluator = null;
        return this;
    }

    @Override
    public IBehaviorBuilder then(
            final Class<? extends IAction> actionType
    ) throws BehaviorException {
        return then(ActionIdentify.toActionId(actionType));
    }

    @Override
    public IBehaviorBuilder then(
            final Class<? extends IAction> actionType,
            final String label,
            final Object... inputs
    ) throws BehaviorException {
        return then(ActionIdentify.toActionId(actionType), label, inputs);
    }

//    private boolean addInterceptor(IAction action, String label) {
//        if (action instanceof IIntercepted) {
//            ActionIdentify interceptorId = ((IIntercepted) action).by();
//            IAction interceptor;
//            interceptor = this._actionRepo.get(interceptorId);
//            if (interceptor == null) {
//                throw BehaviorException.builder()
//                        .errorCode(BehaviorErrors.INTERCEPTOR_NOT_FOUND)
//                        .variables(new BehaviorErrors.InterceptorNotFound()
//                                .actionId(action.getId())
//                                .interceptorId(interceptorId))
//                        .build();
//            }
//            if (! (interceptor instanceof IInterceptor)) {
//                throw BehaviorException.builder()
//                        .errorCode(BehaviorErrors.ACTION_IS_NOT_INTERCEPTOR)
//                        .variables(new BehaviorErrors.ActionIsNotInterceptor()
//                                .actionId(interceptorId))
//                        .build();
//            }
//
//            // The input and output type of dependent action must be same as input type of the action
//            Class<?> interceptorInputType = interceptor.inputType();
//            if (interceptorInputType != action.inputType()) {
//                throw BehaviorException.builder()
//                        .errorCode(BehaviorErrors.INTERCEPTOR_IO_NOT_MATCH_ACTION_INPUT)
//                        .variables(new BehaviorErrors.InterceptorIONotMatchActionInput()
//                                .interceptorId(interceptorId)
//                                .actionId(action.getId())
//                                .interceptorIOType(interceptorInputType)
//                                .actionInputType(action.inputType()))
//                        .build();
//            }
//
//            this._navigator.newNextAction(interceptor, this._lastEvaluator, label);
//            this._lastEvaluator = null;
//            return true;
//        }
//        return false;
//    }

//    @Override
//    public IBehaviorBuilder then(
//            final IAnonymousAction action
//    ) {
//        return then(action, null);
//    }
//
//    @Override
//    public IBehaviorBuilder then(
//            final IAnonymousAction action,
//            final String label
//    ) {
//        ensureNotBuilt();
//        ArgumentChecker.required(action, "action");
//        AnonymousAction aAction = new AnonymousAction(action);
////        if (addInterceptor(aAction, label)) {
////            this._navigator.newNextAction(aAction, this._lastEvaluator, null);
////        } else {
////            this._navigator.newNextAction(aAction, this._lastEvaluator, label);
////        }
//        this._navigator.newNextAction(aAction, this._lastEvaluator, label);
//        this._lastEvaluator = null;
//        return this;
//    }

    @Override
    public IBehaviorBuilder call(
            final IAnonymousCall call
    ) {
        return call(call, null);
    }

    @Override
    public IBehaviorBuilder call(
            final IAnonymousCall call,
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
            final IBehaviorSuccessCall action
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
        this._successAction = action;
        return this;
    }

    @Override
    public IBehaviorBuilder onFailure(
            final IBehaviorFailureCall action
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
                IAction leafAction1 = leafActions.get(idx - 1).action();
                IAction leafAction2 = leafActions.get(idx).action();
                ActionOutputMeta[] outMetas1 = leafAction1.outputMetas();
                ActionOutputMeta[] outMetas2 = leafAction2.outputMetas();
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
                // ensure all action output must have same name and same type
                int unmatchedPos = Looper.on(Range.from(0).to(outMetas1.length).iterator())
                        .filter(pos -> outMetas1[pos].equals(outMetas2[pos]))
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
            });
        }

        // Make all leaf action's next to a exit action
        IAction exit = new EndpointAction(EndpointType.EXIT, leafActions.get(0).outputMetas());
        Looper.on(leafActions).foreach(aHolder -> aHolder.next(new ActionHolder(exit, this)));

        this._iMetas = this._entranceAction.action().inputMetas();
        this._oMetas = exit.outputMetas();
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

    ActionHolder entranceAction() {
        ensureBuilt();
        return this._entranceAction;
    }

    int actionSize() {
        return Looper.on(this._navigator._actions)
                .map(ActionHolder::action)
                .filter(action -> ! (action.getClass().isAssignableFrom(EndpointAction.class)))
                .count();
    }

    private final class EndpointAction implements IAction {

        private final EndpointType _type;
        private final ActionInputMeta[] _inputMetas;
        private final ActionOutputMeta[] _outputMetas;

        private EndpointAction(
                final EndpointType type,
                final ActionInputMeta[] inputMetas
        ) {
            this(type, inputMetas, null);
        }

        private EndpointAction(
                final EndpointType type,
                final ActionOutputMeta[] outputMetas
        ) {
            this(type, null, outputMetas);
        }

        private EndpointAction(
                final EndpointType type,
                final ActionInputMeta[] inputMetas,
                final ActionOutputMeta[] outputMetas
        ) {
            ArgumentChecker.required(type, "type");
            this._type = type;
            this._inputMetas = inputMetas == null ? new ActionInputMeta[0] : inputMetas;
            this._outputMetas = outputMetas == null ? new ActionOutputMeta[0] : outputMetas;
        }

        @Override
        public ActionIdentify getId() {
            return new ActionIdentify(this._type.name(), ActionType.ACTION);
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
    }

    private enum EndpointType {
        ENTRANCE, EXIT
    }

    private final class Navigator implements INavigator {

        private static final int MAX_TRY_ID_COUNT       = 10;

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
                final String label,
                final Object... inputs
        ) throws BehaviorException {
            String actionLabel = label;
            if (StringHelper.isNullOrEmpty(label)) {
                // Generate action label if no label is specified
                String actionId = action.getId().toString();
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
            // create new action holder
            if (action instanceof IIntercepted) {
                this._current = new InterceptedActionHolder(
                        Behavior.this._actionRepo, action, actionLabel, this._current, Behavior.this, evaluator, inputs);
            } else {
                this._current = new ActionHolder(
                        action, actionLabel, this._current, Behavior.this, evaluator, inputs);
            }
            this._actions.add(this._current);


            // Check new action input is matched to current action output
            // The check only on non-anonymous action
//            if (! this._current.action().isAnonymous() && ! action.isAnonymous()) {
//                if (! action.inputType().isAssignableFrom(this._current.action().outputType())) {
//                    throw BehaviorException.builder()
//                            .errorCode(BehaviorErrors.ACTION_IO_MISMATCH)
//                            .variables(new BehaviorErrors.ActionIOMismatch()
//                                    .outputAction(this._current.action().getId())
//                                    .outputType(this._current.action().outputType())
//                                    .inputAction(action.getId())
//                                    .inputType(action.inputType()))
//                            .build();
//                }
//            }
            // Check action label
//            if (! ArgumentChecker.isEmpty(label)) {
//                ActionHolder existingAction = this._labeledActions.get(label);
//                if (existingAction != null) {
//                    throw BehaviorException.builder()
//                            .errorCode(BehaviorErrors.ACTION_LABEL_IS_BIND)
//                            .variables(new BehaviorErrors.ActionLabelIsBind()
//                                    .label(label)
//                                    .actionId(existingAction.action().getId()))
//                            .build();
//                }
//                this._labeledActions.put(label, newAction);
//            }

//            this._current.next(newAction);
//            this._current = newAction;
//            this._actions.add(newAction);
        }
    }
}
