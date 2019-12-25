package uapi.command.internal;

import uapi.command.ParameterMeta;

public class ParamModel extends ParameterMeta {

    private final String _userCmdField;
    private final String _setterName;
    private final int _idx;
    private final String _type;

    public ParamModel(String name, boolean required, String description, int index, String setterName, String userCommandField, String type) {
        super(name, required, description);
        this._idx = index;
        this._setterName = setterName;
        this._userCmdField = userCommandField;
        this._type = type;
    }

    public String getName() {
        return super.name();
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

    public String type() {
        return this._type;
    }
}
