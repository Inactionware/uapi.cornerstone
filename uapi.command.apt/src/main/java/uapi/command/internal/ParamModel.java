package uapi.command.internal;

public class ParamModel extends ParameterMeta {

    private int _idx;

    public ParamModel(String name, boolean required, String description, int index) {
        super(name, required, description);
        this._idx = index;
    }

    public int index() {
        return this._idx;
    }
}
