package uapi.behavior;

public class BehaviorSuccess {

    private final Object[] _inputs;
    private final ActionOutput[] _outputs;

    public BehaviorSuccess(
            final Object[] inputs,
            final ActionOutput[] outputs) {
        this._inputs = inputs;
        this._outputs = outputs;
    }

    public Object inputs() {
        return this._inputs;
    }

    public ActionOutput[] outputs() {
        return this._outputs;
    }
}
