package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event indicate that the behavior is executed finished
 */
public class BehaviorFinishedEvent extends BehaviorTraceEvent {

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object[] behaviorInputs,
            final ActionOutput[] behaviorOutputs,
            final ActionResult behaviorResult,
            final String sourceName
    ) {
        this(executionId, behaviorInputs, behaviorOutputs, behaviorResult, sourceName, null);
    }

    public BehaviorFinishedEvent(
            final ExecutionIdentify executionId,
            final Object[] behaviorInputs,
            final ActionOutput[] behaviorOutputs,
            final ActionResult behaviorResult,
            final String sourceName,
            final Exception exception
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_BEHAVIOR_INPUTS, behaviorInputs);
        set(KEY_CURRENT_OUTPUTS, behaviorOutputs);
        set(KEY_CURRENT_RESULT, behaviorResult);
        set(KEY_SOURCE_NAME, sourceName);
        set(KEY_EX, exception);
    }
}
