/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.Type;
import uapi.behavior.*;
import uapi.common.*;
import uapi.common.Functionals;
import uapi.rx.Looper;

import java.util.LinkedList;
import java.util.List;

/**
 * A ActionHolder holder action and reference its previously action and subsequent actions to construct an actions DGA.
 */
class ActionHolder {

    private static final Functionals.Evaluator ALWAYS_MATCHED = attributed -> true;

    private final Functionals.Evaluator _evaluator;
    private final Behavior _behavior;
    private final IAction _action;
    private final String _label;
    private final List<ActionHolder> _nextActions;
    private final ActionHolder _previousAction;
    private final Object[] _inputs;
    private boolean _hasDefaultNext = false;

    ActionHolder(
            final IAction action,
            final String label,
            final Behavior behavior
    ) {
        this(action, label, null, behavior, null);
    }

    ActionHolder(
            final IAction action,
            final String label,
            final ActionHolder previousAction,
            final Behavior behavior,
            final Functionals.Evaluator evaluator,
            final Object... inputs
    ) {
        ArgumentChecker.required(action, "action");
        ArgumentChecker.required(action.getId(), "action.id");
        ArgumentChecker.required(action.inputMetas(), "action.inputMetas");
        ArgumentChecker.required(action.outputMetas(), "action.outputMetas");
        ArgumentChecker.required(behavior, "behavior");
        if (evaluator == null) {
            this._evaluator = ALWAYS_MATCHED;
        } else {
            this._evaluator = evaluator;
        }
        this._behavior = behavior;
        this._action = action;
        this._label = label;
        this._nextActions = new LinkedList<>();
        this._previousAction = previousAction;
        if (previousAction != null) {
            this._previousAction.next(this);
        }
        this._inputs = inputs;
        verify();
    }

    ActionOutputMeta[] outputMetas() {
        return this._action.outputMetas();
    }

    ActionInputMeta[] inputMetas() {
        return this._action.inputMetas();
    }

    Object[] inputs() {
        return this._inputs;
    }

    /**
     * Return previously action
     *
     * @return  Previously action
     */
    ActionHolder previous() {
        return this._previousAction;
    }

    /**
     * Return the label that bind on this action.
     *
     * @return  The label of this action
     */
    String label() {
        return this._label;
    }

    /**
     * Set next action by specific evaluator
     *
     * @param   actionHolder
     *          The next action
     * @throws  BehaviorException
     *          If this action's output type is not matched next action's input type,
     *          see {@link BehaviorErrors.UnmatchedAction}
     */
    private void next(
            final ActionHolder actionHolder
    ) throws BehaviorException {
        ArgumentChecker.required(actionHolder, "actionHolder");
        this._nextActions.add(actionHolder);
        if (actionHolder._evaluator == ALWAYS_MATCHED) {
            this._hasDefaultNext = true;
        }
    }

    boolean hasNext() {
        return this._nextActions.size() != 0;
    }

    int nextActionSize() {
        return this._nextActions.size();
    }

    IAction action() {
        return this._action;
    }

    /**
     * Find next action by specific input data
     *
     * @param   data
     *          The input data which will be used to evaluate out next action
     * @return  The next action
     * @throws  BehaviorException
     *          If founded next action is not only one, see {@link BehaviorErrors.NotOnlyNextAction}
     */
    ActionHolder findNext(final Object data) throws BehaviorException {
        List<ActionHolder> matchedActions;
        if (data instanceof IAttributed) {
            IAttributed attributed = (IAttributed) data;
            matchedActions = Looper.on(this._nextActions)
                    .filter(actionHolder -> actionHolder._evaluator != ALWAYS_MATCHED)
                    .filter(actionHolder -> actionHolder._evaluator.accept(attributed))
                    .toList();
            if (matchedActions.size() == 1) {
                return matchedActions.get(0);
            } else if (matchedActions.size() > 1) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.NOT_ONLY_NEXT_ACTION)
                        .variables(new BehaviorErrors.NotOnlyNextAction()
                                .actionId(this._action.getId())
                                .data(data))
                        .build();
            }
        }
        matchedActions = Looper.on(this._nextActions)
                .filter(actionHolder -> actionHolder._evaluator == ALWAYS_MATCHED)
                .toList();
        if (matchedActions.size() == 0) {
            return null;
        } else if (matchedActions.size() == 1) {
            return matchedActions.get(0);
        } else {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.NOT_ONLY_NEXT_ACTION)
                    .variables(new BehaviorErrors.NotOnlyNextAction()
                            .actionId(this._action.getId())
                            .data(data))
                    .build();
        }
    }

    void verify() {
        // The action must have a default next action if it has next action
        if (hasNext() && ! this._hasDefaultNext) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.NO_DEFAULT_NEXT_ACTION)
                    .variables(new BehaviorErrors.NoDefaultNextAction()
                            .actionId(this._action.getId())
                            .behaviorId(this._behavior.getId()))
                    .build();
        }

        // Check action input
        ActionInputMeta[] inputMetas = this._action.inputMetas();
        if (inputMetas.length != this._inputs.length) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.INPUT_OUTPUT_COUNT_MISMATCH)
                    .variables(new BehaviorErrors.InputOutputCountMismatch()
                            .inputCount(this._inputs.length)
                            .actionId(this._action.getId())
                            .actionInputCount(inputMetas.length)
                            .behaviorId(this._behavior.getId()))
                    .build();
        }

        Looper.on(this._action.inputMetas()).foreachWithIndex((idx, inputMeta) -> {
            Object input = this._inputs[idx];
            if (input instanceof IOutputReference) {
                IOutputReference outRef = (IOutputReference) input;
                String refAction = outRef.actionLabel();
                boolean foundPrevious = false;
                ActionHolder previous = this._previousAction;
                // Check does referenced action exist or not
                while (previous != null) {
                    if (refAction.equals(previous.label())) {
                        foundPrevious = true;
                        break;
                    }
                    previous = previous.previous();
                }
                if (! foundPrevious) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.REF_ACTION_NOT_EXIST_IN_BEHAVIOR)
                            .variables(new BehaviorErrors.RefActionNotExistInBehavior()
                                    .actionLabel(refAction)
                                    .behaviorId(this._behavior.getId()))
                            .build();
                }
                // Check the referenced action has specific output
                ActionOutputMeta matchedOutMeta = null;
                if (outRef instanceof Behavior.NamedOutput) {
                    String refName = ((Behavior.NamedOutput) outRef).outputName();
                    matchedOutMeta = Looper.on(previous._action.outputMetas())
                            .filter(meta -> meta.name().equals(refName)).first(null);
                } else if (outRef instanceof Behavior.IndexedOutput) {
                    int refIdx = ((Behavior.IndexedOutput) outRef).outputIndex();
                    if (refIdx < previous._action.outputMetas().length) {
                        matchedOutMeta = previous._action.outputMetas()[refIdx];
                    }
                } else {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.UNSUPPORTED_OUTPUT_REF)
                            .variables(new BehaviorErrors.UnsupportedOutputRef()
                                    .referenceType(outRef.getClass()))
                            .build();
                }
                if (matchedOutMeta == null) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.REF_OUTPUT_NOT_FOUND_IN_BEHAVIOR)
                            .variables(new BehaviorErrors.RefOutputNotFoundInBehavior()
                                    .behaviorId(this._behavior.getId())
                                    .outputReference(outRef)
                                    .referenceActionId(previous._action.getId()))
                            .build();
                }
                // Check the referenced action output type does match required input type
                if (! Type.isAssignable(matchedOutMeta.type(), inputMeta.type())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.INPUT_OUTPUT_TYPE_MISMATCH)
                            .variables(new BehaviorErrors.InputOutputTypeMismatch()
                                    .outputActionId(previous._action.getId())
                                    .outputType(matchedOutMeta.type())
                                    .outputName(matchedOutMeta.name())
                                    .inputActionId(this._action.getId())
                                    .inputType(inputMeta.type()))
                            .build();
                }
            } else {
                if (input == null || ! inputMeta.type().isAssignableFrom(input.getClass())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.INPUT_OBJECT_TYPE_MISMATCH)
                            .variables(new BehaviorErrors.InputObjectTypeMismatch()
                                    .inputObject(input == null ? "null" : input)
                                    .inputObjectType(input == null ? null : input.getClass())
                                    .actionId(this._action.getId())
                                    .actionInputType(inputMeta.type()))
                            .build();
                }
            }
        });
    }

    void execute(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context
    ) throws Exception {
        this._action.process(inputs, outputs, context);
    }
}
