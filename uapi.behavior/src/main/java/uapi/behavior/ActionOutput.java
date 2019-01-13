package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * The class hold output of action
 */
public class ActionOutput {

    private final ActionOutputMeta _meta;
    private Object _output;

    public ActionOutput(
            final ActionOutputMeta meta
    ) {
        ArgumentChecker.required(meta, "meta");
        this._meta = meta;
    }

    public String name() {
        return this._meta.name();
    }

    public void set(final Object output) {
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
                            .type(this._meta.type()))
                    .build();
        }
        this._output = output;
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) this._output;
    }
}
