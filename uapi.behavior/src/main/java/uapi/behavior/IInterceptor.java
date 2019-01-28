package uapi.behavior;

import uapi.rx.Looper;

/**
 * The action is used to intercept other action.
 */
public interface IInterceptor extends IAction {

    @Override
    default ActionOutputMeta[] outputMetas() {
        ActionInputMeta[] inputMetas = this.inputMetas();
        if (inputMetas.length == 0) {
            return new ActionOutputMeta[0];
        }
        return Looper.on(inputMetas)
                .map(inputMeta -> new ActionOutputMeta(inputMeta.type()))
                .toArray();
    }
}
