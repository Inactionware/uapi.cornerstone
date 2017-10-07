package uapi.command.internal;

public class ParamModel extends ParameterMeta {

    private final String _fieldName;
    private final int _idx;

    public ParamModel(String name, boolean required, String description, int index, String fieldName) {
        super(name, required, description);
        this._idx = index;
        this._fieldName = fieldName;
    }

    public int index() {
        return this._idx;
    }

    public String fieldName() {
        return this._fieldName;
    }
}
