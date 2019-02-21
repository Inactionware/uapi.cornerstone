package uapi.behavior;

import uapi.IPartibleIdentify;
import uapi.InvalidArgumentException;
import uapi.common.ArgumentChecker;

/**
 * Identify for action and behavior
 */
public class ActionIdentify implements IPartibleIdentify<String> {

    protected static final String SEPARATOR   = "@";

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
            ActionType type;
            try {
                type = ActionType.valueOf(combined[1]);
            } catch (IllegalArgumentException ex) {
                throw new InvalidArgumentException(ex);
            }
            return new ActionIdentify(combined[0], type);
        } else {
            throw new InvalidArgumentException(id, InvalidArgumentException.InvalidArgumentType.FORMAT);
        }
    }

    public static ActionIdentify toActionId(Class<?> actionType) {
        ArgumentChecker.required(actionType, "actionType");
        String className = actionType.getCanonicalName();
        className = className.replace('$', (char) 0);
        if (IAction.class.isAssignableFrom(actionType)) {
            return new ActionIdentify(className, ActionType.ACTION);
        } else if (IBehavior.class.isAssignableFrom(actionType)) {
            return new ActionIdentify(className, ActionType.BEHAVIOR);
        } else {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.INCOMPATIBLE_ACTION_TYPE)
                    .variables(new BehaviorErrors.IncompatibleActionType()
                            .type(actionType))
                    .build();
        }
    }

    public ActionIdentify(
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
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (! (other instanceof ActionIdentify)) {
            return false;
        }
        ActionIdentify actionId = (ActionIdentify) other;
        return this.getId().equals(actionId.getId());
    }

    @Override
    public String toString() {
        return getId();
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
