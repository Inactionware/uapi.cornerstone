package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * A event indicate that the behavior is executed finished
 */
public class BehaviorFinishedEvent extends BehaviorTraceEvent {

    public static final String KEY_BEHAVIOR_OUTPUTS = "BehaviorOutputs";

    public BehaviorFinishedEvent(
            final String sourceName,
            final ExecutionIdentify executionId,
            final Object[] behaviorInputs,
            final ActionOutput[] behaviorOutputs,
            final Exception exception
    ) {
        super(sourceName);
        ArgumentChecker.required(executionId, "executionId");
        set(KEY_EXECUTION_ID, executionId);
        set(KEY_BEHAVIOR_INPUTS, behaviorInputs);
        set(KEY_BEHAVIOR_OUTPUTS, behaviorOutputs);
        set(KEY_SOURCE_NAME, sourceName);
        set(KEY_EX, exception);
    }

    public ActionOutput[] behaviorOutputs() {
        return (ActionOutput[]) get(KEY_BEHAVIOR_OUTPUTS);
    }
}
