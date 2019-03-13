package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * The class hold output of action
 */
public class ActionOutput<T> {

    private final ActionIdentify _actionId;
    private final ActionOutputMeta _meta;
    private T _output;

    public ActionOutput(
            final ActionIdentify actionId,
            final ActionOutputMeta meta
    ) {
        ArgumentChecker.required(actionId, "actionId");
        ArgumentChecker.required(meta, "meta");
        this._actionId = actionId;
        this._meta = meta;
    }

    public void set(final T output) {
        ArgumentChecker.required(output, "output");
        if (this._output != null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_OUTPUT_SET_TWICE)
                    .variables(new BehaviorErrors.ActionOutputSetTwice()
                            .name(this._meta.name()))
                    .build();
        }
        if (! this._meta.type().isInstance(output)) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_OUTPUT_TYPE_NOT_MATCHED)
                    .variables(new BehaviorErrors.ActionOutputTypeNotMatched()
                            .output(output)
                            .actionId(this._actionId)
                            .outputName(this._meta.name())
                            .requiredType(this._meta.type()))
                    .build();
        }
        this._output = output;
    }

    public T get() {
        return this._output;
    }

    public ActionOutputMeta meta() {
        return this._meta;
    }
}
