package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.event.AttributedEvent;

/**
 * A event is raised during behavior execution
 */
public class BehaviorExecutingEvent extends AttributedEvent implements IBehaviorTraceEvent {

    private static final String KEY_ACTION_NAME     = "ActionName";

    public BehaviorExecutingEvent(
            final String topic,
            final String executionId,
            final String actionName,
            final String behaviorName,
            final String originalData,
            final Object data
    ) {
        super(topic);
        ArgumentChecker.required(executionId, "executionId");
        ArgumentChecker.required(actionName, "actionName");
        ArgumentChecker.required(behaviorName, "behaviorName");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ACTION_NAME, actionName);
        set(KEY_BEHAVIOR_NAME, behaviorName);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
    }

    public String actionName() {
        return (String) get(KEY_ACTION_NAME);
    }
}
