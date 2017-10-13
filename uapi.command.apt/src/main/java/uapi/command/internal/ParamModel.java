package uapi.command.internal;

public class ParamModel extends ParameterMeta {

    private final String _userCmdField;
    private final String _setterName;
    private final int _idx;

    public ParamModel(String name, boolean required, String description, int index, String setterName, String userCommandField) {
        super(name, required, description);
        this._idx = index;
        this._setterName = setterName;
        this._userCmdField = userCommandField;
    }

    public int index() {
        return this._idx;
    }

    public String userCommandField() {
        return this._userCmdField;
    }

    public String setterName() {
        return this._setterName;
    }
}
