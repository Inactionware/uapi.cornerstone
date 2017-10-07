package uapi.command.internal;

public class OptionModel extends OptionMeta {

    private final String _fieldName;

    public OptionModel(String name, char sortName, String argument, String description, String fieldName) {
        super(name, sortName, argument, description);
        this._fieldName = fieldName;
    }

    public String fieldName() {
        return this._fieldName;
    }
}
