package uapi.behavior.internal;

import uapi.behavior.BehaviorErrors;
import uapi.behavior.BehaviorException;
import uapi.behavior.IAction;
import uapi.common.ArgumentChecker;
import uapi.common.Functionals;
import uapi.common.IAttributed;
import uapi.rx.Looper;

import java.util.LinkedList;
import java.util.List;

/**
 * A ActionHolder holder action and reference its previously action and subsequent actions to construct an actions DGA.
 */
class ActionHolder {

    private static final Functionals.Evaluator ALWAYS_MATCHED = attributed -> true;

    private final Functionals.Evaluator _evaluator;
    private final IAction _action;
    private final List<ActionHolder> _nextActions;

    ActionHolder(final IAction action) {
        this(action, null);
    }

    ActionHolder(
            final IAction action,
            final Functionals.Evaluator evaluator
    ) {
        ArgumentChecker.required(action, "action");
        ArgumentChecker.required(action.getId(), "action.id");
        ArgumentChecker.required(action.inputType(), "action.inputType");
        ArgumentChecker.required(action.outputType(), "action.outputType");
        if (evaluator == null) {
            this._evaluator = ALWAYS_MATCHED;
        } else {
            this._evaluator = evaluator;
        }
        this._action = action;
        this._nextActions = new LinkedList<>();
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
//        if (! this._action.outputType().equals(actionHolder.action().inputType())) {
//            throw BehaviorException.builder()
//                    .errorCode(BehaviorErrors.UNMATCHED_ACTION)
//                    .variables(new BehaviorErrors.UnmatchedAction()
//                            .outputType(this._action.outputType().getCanonicalName())
//                            .outputAction(this._action.getId())
//                            .inputType(actionHolder.action().inputType().getCanonicalName())
//                            .inputAction(actionHolder.action().getId()))
//                    .build();
//        }
        this._nextActions.add(actionHolder);
    }

    boolean hasNext() {
        return this._nextActions.size() != 0;
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
}
