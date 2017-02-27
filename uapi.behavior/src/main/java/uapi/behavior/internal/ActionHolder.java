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
        if (evaluator == null) {
            this._evaluator = ALWAYS_MATCHED;
        } else {
            this._evaluator = evaluator;
        }
        this._action = action;
        this._nextActions = new LinkedList<>();
    }

    void next(final IAction action) {
        ArgumentChecker.required(action, "action");
        next(action, null);
    }

    void next(
            final IAction action,
            final Functionals.Evaluator evaluator
    ) throws BehaviorException {
        if (! this._action.outputType().equals(action.inputType())) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.UNMATCHED_ACTION)
                    .variables(new BehaviorErrors.UnmatchedAction()
                            .outputType(this._action.outputType().getCanonicalName())
                            .outputAction(this._action.getId().toString())
                            .inputType(action.inputType().getCanonicalName())
                            .inputAction(action.getId().toString()))
                    .build();
        }
        this._nextActions.add(new ActionHolder(action, evaluator));
    }

    boolean hasNext() {
        return this._nextActions.size() != 0;
    }

    IAction action() {
        return this._action;
    }

    ActionHolder findNext(Object data) {
        ActionHolder next;
        if (data instanceof IAttributed) {
            IAttributed attributed = (IAttributed) data;
            next = Looper.on(this._nextActions)
                    .filter(actionHolder -> actionHolder._evaluator.accept(attributed))
                    .first();
        } else {
            if (this._nextActions.size() != 1) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.NOT_ONLY_POST_ACTION)
                        .variables(new BehaviorErrors.NotOnlyPostAction()
                                .actionName(this._action.getId().toString()))
                        .build();
            }
            next = this._nextActions.get(0);
        }
        return next;
    }
}
