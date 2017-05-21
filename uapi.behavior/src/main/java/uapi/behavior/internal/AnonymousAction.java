package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.ActionIdentify;
import uapi.behavior.IAction;
import uapi.behavior.IAnonymousAction;
import uapi.behavior.IExecutionContext;
import uapi.common.ArgumentChecker;

/**
 * Created by min on 2017/5/21.
 */
public class AnonymousAction implements IAction<Object, Object> {

    private final IAnonymousAction _action;

    public AnonymousAction(IAnonymousAction action) {
        ArgumentChecker.required(action, "action");
        this._action = action;
    }

    @Override
    public Object process(Object input, IExecutionContext context) {
        Object output = null;
        if (this._action instanceof IAnonymousAction.IContextIgnorant) {
            output = ((IAnonymousAction.IContextIgnorant) this._action).accept(input);
        } else if (this._action instanceof IAnonymousAction.IContextAware) {
            output = ((IAnonymousAction.IContextAware) this._action).accept(input, context);
        } else {
            throw new GeneralException("error");
        }
        return output;
    }

    @Override
    public Class<Object> inputType() {
        return Object.class;
    }

    @Override
    public Class<Object> outputType() {
        return Object.class;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public ActionIdentify getId() {
        return ActionIdentify.toActionId(this._action.getClass());
    }
}
