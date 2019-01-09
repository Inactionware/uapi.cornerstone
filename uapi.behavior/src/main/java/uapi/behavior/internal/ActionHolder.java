package uapi.behavior.internal;

import uapi.behavior.*;
import uapi.common.*;
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

    private ActionHolder _previousAction;

    ActionHolder(
            final IAction action,
            final Behavior behavior
    ) {
        this(action, null, behavior, null);
    }

    ActionHolder(
            final IAction action,
            final String label,
            final Behavior behavior,
            final Functionals.Evaluator evaluator
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
    }

    ActionOutputMeta[] outputMetas() {
        return this._action.outputMetas();
    }

    ActionInputMeta[] inputMetas() {
        return this._action.inputMetas();
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
    void next(
            final ActionHolder actionHolder
    ) throws BehaviorException {
        ArgumentChecker.required(actionHolder, "actionHolder");
        this._nextActions.add(actionHolder);
        actionHolder._previousAction = this;
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

    void verifyOutput(final String[] inputs) {
        Looper.on(inputs).foreach(input -> {
            ArgumentChecker.required(input, "input");
            Pair<String, String> inputRef = ActionInputMeta.parse(input);
            String refLabel = inputRef.getLeftValue();
            String refName = inputRef.getRightValue();

            boolean foundPrevious = false;
            ActionHolder previous = this;
            while (previous != null) {
                if (refLabel.equals(previous.label())) {
                    foundPrevious = true;
                    break;
                }
                previous = previous.previous();
            }
            if (! foundPrevious) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.REF_ACTION_NOT_EXIST_IN_BEHAVIOR)
                        .variables(new BehaviorErrors.RefActionNotExistInBehavior()
                                .actionLabel(refLabel)
                                .behaviorId(this._behavior.getId()))
                        .build();
            }
            boolean found = Looper.on(previous._action.outputMetas()).filter(meta -> meta.name().equals(refName)).first() != null;
            if (! found) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.NO_OUTPUT_IN_ACTION)
                        .variables(new BehaviorErrors.NoOutputInAction()
                                .outputName(refName)
                                .actionId(previous._action.getId()))
                        .build();
            }
        });
    }
}
