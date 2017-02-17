package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.event.AttributedEvent;

/**
 * A event indicate that the behavior is executed finished
 */
public class BehaviorFinishedEvent extends AttributedEvent implements IBehaviorTraceEvent {

    private static final String KEY_EXECUTION_ID    = "ExecutionId";
    private static final String KEY_ORIGINAL_DATA   = "OriginalData";
    private static final String KEY_DATA            = "Data";

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data
    ) {
        super(IBehaviorTraceEvent.TOPIC);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
    }
}
