package uapi.command;

import uapi.command.IParameterMeta;

public class ParameterMeta implements IParameterMeta {

    private final String _name;
    private final boolean _required;
    private final String _desc;

    public ParameterMeta(String name, boolean required, String description) {
        this._name = name;
        this._required = required;
        this._desc = description;
    }

    @Override
    public String name() {
        return this._name;
    }

    @Override
    public boolean required() {
        return this._required;
    }

    @Override
    public String description() {
        return this._desc;
    }
}
