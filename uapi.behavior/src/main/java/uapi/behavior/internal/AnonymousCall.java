package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;

/**
 * An anonymous call
 */
public class AnonymousCall implements IAction {

    private final IAnonymousCall _action;

    public AnonymousCall(IAnonymousCall action) {
        ArgumentChecker.required(action, "action");
        this._action = action;
    }

    @Override
    public void process(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context) {
        try {
            this._action.accept(context);
        } catch (Exception ex) {
            throw new GeneralException(ex);
        }
    }

    @Override
    public ActionInputMeta[] inputMetas() {
        return new ActionInputMeta[0];
    }

    @Override
    public ActionOutputMeta[] outputMetas() {
        return new ActionOutputMeta[0];
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
