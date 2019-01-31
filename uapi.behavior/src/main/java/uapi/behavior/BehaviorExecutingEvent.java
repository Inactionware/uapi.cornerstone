package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event is raised during behavior execution
 */
public class BehaviorExecutingEvent extends BehaviorTraceEvent {

    private static final String KEY_ACTION_ID   = "ActionId";

    public BehaviorExecutingEvent(
            final ExecutionIdentify executionId,
            final Object[] behaviorInputs,
            final ActionOutput[] actionOutputs,
            final ActionResult actionResult,
            final String sourceName
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_BEHAVIOR_INPUTS, behaviorInputs);
        set(KEY_CURRENT_OUTPUTS, actionOutputs);
        set(KEY_CURRENT_RESULT, actionResult);
        set(KEY_SOURCE_NAME, sourceName);
    }

    public ActionIdentify actionId() {
        return ((ActionResult) get(KEY_CURRENT_RESULT)).actionId();
    }
}
