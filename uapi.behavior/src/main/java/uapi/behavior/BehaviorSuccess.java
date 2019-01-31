package uapi.behavior;

public class BehaviorSuccess {

    private final ActionResult _result;
    private final ActionOutput[] _outputs;

    public BehaviorSuccess(
            final ActionResult result,
            final ActionOutput[] outputs) {
        this._result = result;
        this._outputs = outputs;
    }

    public ActionResult result() {
        return this._result;
    }

    public ActionOutput[] outputs() {
        return this._outputs;
    }
}
