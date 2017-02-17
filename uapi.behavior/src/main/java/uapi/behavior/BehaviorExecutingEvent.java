package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.event.AttributedEvent;

/**
 * A event is raised during behavior execution
 */
public class BehaviorExecutingEvent extends AttributedEvent implements IBehaviorTraceEvent {

    private static final String KEY_ACTION_ID   = "ActionId";

    public BehaviorExecutingEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data,
            final ActionIdentify actionId
    ) {
        super(IBehaviorTraceEvent.TOPIC);
        ArgumentChecker.required(executionId, "executionId");
        ArgumentChecker.required(actionId, "actionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ACTION_ID, actionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
    }

    public ActionIdentify actionId() {
        return (ActionIdentify) get(KEY_ACTION_ID);
    }
}
