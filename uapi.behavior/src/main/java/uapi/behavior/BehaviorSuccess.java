package uapi.behavior;

public class BehaviorSuccess {

    private final Object _result;

    public BehaviorSuccess(final Object result) {
        this._result = result;
    }

    public Object result() {
        return this._result;
    }
}
