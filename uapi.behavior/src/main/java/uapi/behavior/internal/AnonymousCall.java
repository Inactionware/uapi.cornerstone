package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;

/**
 * An anonymous call
 */
public class AnonymousCall<I> implements IAction {

    private final IAnonymousCall<I> _action;

    public AnonymousCall(IAnonymousCall<I> action) {
        ArgumentChecker.required(action, "action");
        this._action = action;
    }

    @Override
    public ActionResult process(Object[] inputs, ActionOutput[] outputs, IExecutionContext context) {
        try {
            this._action.accept(input, context);
        } catch (Exception ex) {
            throw new GeneralException(ex);
        }
        return null;
    }

    @Override
    public Class inputType() {
        return Object.class;
    }

    @Override
    public Class<Void> outputType() {
        return Void.class;
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
