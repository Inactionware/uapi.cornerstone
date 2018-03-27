package uapi.behavior;

import uapi.common.ArgumentChecker;

public class BehaviorFailure {

    private final ActionIdentify _failureAction;
    private final Object _failureInput;
    private final Exception _cause;

    public BehaviorFailure(
            final ActionIdentify failureAction,
            final Object failureInput,
            final Exception cause
    ) {
        ArgumentChecker.required(failureAction, "failureAction");
        ArgumentChecker.required(failureInput, "failureInput");
        ArgumentChecker.required(cause, "cause");

        this._failureAction = failureAction;
        this._failureInput  = failureInput;
        this._cause = cause;
    }

    public ActionIdentify failureAction() {
        return this._failureAction;
    }

    public Object failureInput() {
        return this._failureInput;
    }

    public Exception cause() {
        return this._cause;
    }
}
