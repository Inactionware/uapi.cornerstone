package uapi.behavior;

import uapi.common.ArgumentChecker;

public class BehaviorFailure {

    private final ActionIdentify _failureAction;
    private final Object[] _failureInputs;
    private final Exception _cause;

    public BehaviorFailure(
            final ActionIdentify failureAction,
            final Object[] failureInputs,
            final Exception cause
    ) {
        ArgumentChecker.required(failureAction, "failureAction");
        ArgumentChecker.required(failureInputs, "failureInput");
        ArgumentChecker.required(cause, "cause");

        this._failureAction = failureAction;
        this._failureInputs = failureInputs;
        this._cause = cause;
    }

    public ActionIdentify failureAction() {
        return this._failureAction;
    }

    public Object[] failureInputs() {
        return this._failureInputs;
    }

    public Exception cause() {
        return this._cause;
    }
}
