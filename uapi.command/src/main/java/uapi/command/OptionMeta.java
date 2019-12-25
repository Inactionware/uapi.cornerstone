package uapi.command;

import uapi.command.IOptionMeta;
import uapi.command.OptionType;
import uapi.common.ArgumentChecker;

public class OptionMeta implements IOptionMeta {

    private final String _name;
    private final char _sname;
    private final String _arg;
    private final String _desc;
    private final OptionType _type;

    public OptionMeta(
            final String name,
            final char sortName,
            final String argument,
            final String description,
            final OptionType type
    ) {
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(sortName, "sortName");
        ArgumentChecker.required(description, "description");

        this._name = name;
        this._sname = sortName;
        this._arg = argument;
        this._desc = description;
        this._type = type;
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

    public OptionType type() {
        return this._type;
    }
}
