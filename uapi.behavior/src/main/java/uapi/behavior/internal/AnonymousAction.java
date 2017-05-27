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
public class AnonymousAction<I, O> implements IAction<I, O> {

    private final IAnonymousAction<I, O> _action;

    public AnonymousAction(IAnonymousAction<I, O> action) {
        ArgumentChecker.required(action, "action");
        this._action = action;
    }

    @Override
    public O process(I input, IExecutionContext context) {
        O output = null;
        try {
            output = this._action.accept(input, context);
        } catch (Exception ex) {
            throw new GeneralException(ex);
        }
        return output;
    }

    @Override
    public Class inputType() {
        return Object.class;
    }

    @Override
    public Class outputType() {
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
