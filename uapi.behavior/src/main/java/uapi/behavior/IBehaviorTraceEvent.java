package uapi.behavior;

import uapi.behavior.internal.ExecutionIdentify;
import uapi.common.IAttributed;
import uapi.event.IEvent;

/**
 * The interface contains methods which should be used for trace behavior execution
 */
public interface IBehaviorTraceEvent extends IAttributed, IEvent {

    String TOPIC                = "BehaviorTrace";
    String KEY_EXECUTION_ID     = "ExecutionId";
    String KEY_ORIGINAL_DATA    = "OriginalData";
    String KEY_DATA             = "Data";

    @Override
    default String topic() {
        return TOPIC;
    }

    /**
     * Get this event associated behavior execution id.
     *
     * @return  The behavior execution id
     */
    default ExecutionIdentify executionId() {
        return (ExecutionIdentify) get(KEY_EXECUTION_ID);
    }

    /**
     * Get this event associated behavior name.
     *
     * @return  The behavior name
     */
    default String behaviorName() {
        return executionId().getName();
    }

    /**
     * Get original input data of this behavior
     *
     * @return  The original input data
     */
    default Object originalData() {
        return get(KEY_ORIGINAL_DATA);
    }

    /**
     * Get last outputted data of this behavior
     *
     * @return  The last outputted data
     */
    default Object data() {
        return get(KEY_DATA);
    }
}
