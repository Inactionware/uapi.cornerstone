package uapi.behavior.internal;

import uapi.behavior.*;
import uapi.common.ArgumentChecker;

/**
 * Created by min on 2017/5/21.
 */
public abstract class AnonymousAction<T> implements IAction {

    private final T _action;
    private final ActionIdentify _behaviorId;

    public AnonymousAction(
            final T action,
            final ActionIdentify behaviorId)
    {
        ArgumentChecker.required(action, "action");
        ArgumentChecker.required(behaviorId, "behaviorId");
        this._action = action;
        this._behaviorId = behaviorId;
    }

    public T action() {
        return this._action;
    }

    public ActionIdentify behaviorId() {
        return this._behaviorId;
    }

//    @Override
//    public ActionResult process(
//            final Object[] inputs,
//            final ActionOutput[] outputs,
//            final IExecutionContext context
//    ) {
//        ActionResult result = null;
//        try {
//            result = this._action.accept(context);
//        } catch (Exception ex) {
//            throw new GeneralException(ex);
//        }
//        return result;
//    }

    @Override
    public ActionInputMeta[] inputMetas() {
        return new ActionInputMeta[0];
    }

    @Override
    public ActionOutputMeta[] outputMetas() {
        return new ActionOutputMeta[] {
                new ActionOutputMeta(BehaviorEvent.class)
        };
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
