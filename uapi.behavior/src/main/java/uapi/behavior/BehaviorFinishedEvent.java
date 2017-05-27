package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event indicate that the behavior is executed finished
 */
public class BehaviorFinishedEvent extends BehaviorTraceEvent {

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data,
            final String sourceName
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
    }

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data,
            final String sourceName,
            final Exception exception
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
        set(KEY_EX, exception);
    }
}
