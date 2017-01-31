package uapi.behavior;

import uapi.IPartibleIdentify;
import uapi.InvalidArgumentException;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;

/**
 * Identify for action and behavior
 */
public class ActionIdentify implements IPartibleIdentify<String> {

    private static final String SEPARATOR   = "@";

    private final String _name;
    private final ActionType _type;
    private final String _id;

    public static ActionIdentify parse(final String id) {
        ArgumentChecker.required(id, "id");
        if (id.indexOf(SEPARATOR) <= 0) {
            throw new InvalidArgumentException(id, InvalidArgumentException.InvalidArgumentType.FORMAT);
        }
        String[] combined = id.split(SEPARATOR);
        if (combined.length == 2) {
            ActionType type = ActionType.valueOf(combined[1]);
            return new ActionIdentify(combined[0], type);
        } else {
            throw new InvalidArgumentException(id, InvalidArgumentException.InvalidArgumentType.FORMAT);
        }
    }

    private ActionIdentify(
            final String name,
            final ActionType type
    ) {
        ArgumentChecker.required(name, "id");
        ArgumentChecker.required(type, "type");
        this._name = name;
        this._type = type;
        this._id = name + SEPARATOR + type.name();
    }

    @Override
    public String getId() {
        return this._id;
    }

    @Override
    public Object[] getParts() {
        return new Object[] { this._name, this._type };
    }

    public String getName() {
        return this._name;
    }

    public ActionType getType() {
        return this._type;
    }
}
