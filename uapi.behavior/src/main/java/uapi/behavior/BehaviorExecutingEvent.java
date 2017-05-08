package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event is raised during behavior execution
 */
public class BehaviorExecutingEvent extends BehaviorTraceEvent {

    private static final String KEY_ACTION_ID   = "ActionId";

    public BehaviorExecutingEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data,
            final ActionIdentify actionId,
            final String responsibleName
    ) {
        super();
        ArgumentChecker.required(executionId, "executionId");
        ArgumentChecker.required(actionId, "actionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ACTION_ID, actionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
        set(KEY_RESP_NAME, responsibleName);
    }

    public ActionIdentify actionId() {
        return (ActionIdentify) get(KEY_ACTION_ID);
    }
}
