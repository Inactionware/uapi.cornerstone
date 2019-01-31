package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.common.Attributed;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * The class hold output of action
 */
public class ActionOutput extends Attributed {

    private final ActionIdentify _actionId;
    private final Map<String, ActionOutputMeta> _metas;

    public ActionOutput(
            final ActionIdentify actionId
    ) {
        this(actionId, new ActionOutputMeta[0]);
    }

    public ActionOutput(
            final ActionIdentify actionId,
            final ActionOutputMeta[] metas
    ) {
        ArgumentChecker.required(actionId, "actionId");
        ArgumentChecker.required(metas, "metas");
        this._actionId = actionId;
        this._metas = new HashMap<>();
        // Duplicated action output meta was checked in ResponsibleRegistry::addAction method
        Looper.on(metas).foreach(meta -> this._metas.put(meta.name(), meta));
    }

    @Override
    public Object set(
            final Object name,
            final Object output) {
        ArgumentChecker.required(name, "name");
        ArgumentChecker.required(output, "output");
        ActionOutputMeta meta = this._metas.get(name);
        if (meta == null) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.INCORRECT_ACTION_OUTPUT_NAME)
                    .variables(new BehaviorErrors.IncorrectActionOutputName()
                            .outputName((String) name)
                            .actionId(this._actionId))
                    .build();
        }
        if (! meta.type().isInstance(output)) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.ACTION_OUTPUT_TYPE_NOT_MATCHED)
                    .variables(new BehaviorErrors.ActionOutputTypeNotMatched()
                            .output(output)
                            .outputType(output.getClass())
                            .actionId(this._actionId)
                            .outputName((String) name)
                            .requiredType(meta.type()))
                    .build();
        }
        return super.set(name, output);
    }
}
