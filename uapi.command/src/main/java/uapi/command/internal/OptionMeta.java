package uapi.command.internal;

import uapi.common.ArgumentChecker;

public class OptionMeta {

    private final String _name;
    private final char _sname;
    private final String _arg;
    private final String _desc;

    public OptionMeta(
            final String name,
            final char sortName,
            final String argument,
            final String description
    ) {
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(sortName, "sortName");
        ArgumentChecker.required(argument, "argument");
        ArgumentChecker.required(description, "description");

        this._name = name;
        this._sname = sortName;
        this._arg = argument;
        this._desc = description;
    }

    public String name() {
        return this._name;
    }

    public char shortName() {
        return this._sname;
    }

    public String argument() {
        return this._arg;
    }

    public String description() {
        return this._desc;
    }
}
