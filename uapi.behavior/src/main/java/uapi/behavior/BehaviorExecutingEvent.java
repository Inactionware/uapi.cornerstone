package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event is raised during behavior execution
 */
public class BehaviorExecutingEvent extends BehaviorTraceEvent {

    public BehaviorExecutingEvent(
            final String sourceName,
            final ExecutionIdentify executionId,
            final ActionIdentify actionId,
            final Object[] actionInputs,
            final ActionOutput[] actionOutputs,
            final Object[] behaviorInputs
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_BEHAVIOR_INPUTS, behaviorInputs);
        set(KEY_CURRENT_ACTION_ID, actionId);
        set(KEY_CURRENT_INPUTS, actionInputs);
        set(KEY_CURRENT_OUTPUTS, actionOutputs);
        set(KEY_SOURCE_NAME, sourceName);
    }
}
