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
            final ActionResult result,
            final ActionIdentify actionId,
            final String sourceName
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        ArgumentChecker.required(actionId, "actionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ACTION_ID, actionId);
        set(KEY_BEHAVIOR_INPUTS, behaviorInputs);
        set(KEY_RESULT, result);
    }

    public ActionIdentify actionId() {
        return (ActionIdentify) get(KEY_ACTION_ID);
    }
}
