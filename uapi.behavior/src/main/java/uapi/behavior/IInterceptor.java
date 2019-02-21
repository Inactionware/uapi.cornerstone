package uapi.behavior;

/**
 * The action is used to intercept other action.
 */
public interface IInterceptor extends IAction {

    @Override
    default ActionOutputMeta[] outputMetas() {
        return new ActionOutputMeta[0];
    }
}
