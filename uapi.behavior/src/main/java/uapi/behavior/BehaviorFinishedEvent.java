package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event indicate that the behavior is executed finished
 */
public class BehaviorFinishedEvent extends BehaviorTraceEvent {

//    private static final String KEY_EXECUTION_ID    = "ExecutionId";
//    private static final String KEY_ORIGINAL_DATA   = "OriginalData";
//    private static final String KEY_DATA            = "Data";

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object originalData,
            final Object data,
            final String responsibleName
    ) {
        super();
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_ORIGINAL_DATA, originalData);
        set(KEY_DATA, data);
        set(KEY_RESP_NAME, responsibleName);
    }
}
