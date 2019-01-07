package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;

/**
 * Created by min on 2017/5/21.
 */
public class AnonymousAction implements IAction {

    private final IAnonymousAction _action;

    public AnonymousAction(IAnonymousAction action) {
        ArgumentChecker.required(action, "action");
        this._action = action;
    }

    @Override
    public ActionResult process(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context
    ) {
        ActionResult result = null;
        try {
            result = this._action.accept(context);
        } catch (Exception ex) {
            throw new GeneralException(ex);
        }
        return result;
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
