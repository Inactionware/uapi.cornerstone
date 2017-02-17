package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.InvalidArgumentException;
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

    void next(IAction action) {
        ArgumentChecker.required(action, "action");
        next(action, null);
    }

    void next(IAction action, Functionals.Evaluator evaluator) {
        if (! this._action.outputType().equals(action.inputType())) {
            throw new InvalidArgumentException(
                    "Unmatched output type {} of action {} to input type {} of action {}",
                    this._action.outputType(), this._action.getId(),
                    action.outputType(), action.getId());
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
                throw new GeneralException(
                        "Found zero or more post action when handler data without attributes - {}",
                        this._action.getId());
            }
            next = this._nextActions.get(0);
        }
        return next;
    }
}
