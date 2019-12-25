package uapi.command.internal;

import uapi.command.OptionMeta;
import uapi.command.OptionType;

public class OptionModel extends OptionMeta {

    private final String _fieldName;
    private final String _userCmdField;
    private final String _setterName;

    public OptionModel(String name, char sortName, String argument, String description, OptionType type, String fieldName, String setterName, String userCommandField) {
        super(name, sortName, argument, description, type);
        this._fieldName = fieldName;
        this._userCmdField = userCommandField;
        this._setterName = setterName;
    }

    public String fieldName() {
        return this._fieldName;
    }

    public String userCommandField() {
        return this._userCmdField;
    }

    public String setterName() {
        return this._setterName;
    }

    public boolean isBoolean() {
        return type() == OptionType.Boolean;
    }

    public boolean isString() {
        return type() == OptionType.String;
    }
}
