package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;

public class BehaviorFailure {

    private final ActionIdentify _failureAction;
    private final Object[] _failureInputs;
    private final String _msg;
    private final Exception _cause;

    public BehaviorFailure(
            final ActionIdentify failureAction,
            final Object[] failureInputs,
            final String message
    ) {
        this(failureAction, failureInputs, message, null);
    }

    public BehaviorFailure(
            final ActionIdentify failureAction,
            final Object[] failureInputs,
            final Exception cause
    ) {
        this(failureAction, failureInputs, null, cause);
    }

    public BehaviorFailure(
            final ActionIdentify failureAction,
            final Object[] failureInputs,
            final String message,
            final Exception cause
    ) {
        ArgumentChecker.required(failureAction, "failureAction");
        ArgumentChecker.required(failureInputs, "failureInput");

        this._failureAction = failureAction;
        this._failureInputs = failureInputs;
        this._cause = cause;
        if (StringHelper.isNullOrEmpty(message) && cause != null) {
            this._msg = cause.getMessage();
        } else {
            this._msg = null;
        }
    }

    public ActionIdentify failureAction() {
        return this._failureAction;
    }

    public Object[] failureInputs() {
        return this._failureInputs;
    }

    public String message() {
        return this._msg;
    }

    public Exception cause() {
        return this._cause;
    }
}
