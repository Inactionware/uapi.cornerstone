package uapi.behavior;

import uapi.event.AttributedEvent;

/**
 * The interface contains methods which should be used for trace behavior execution
 */
public abstract class BehaviorTraceEvent extends BehaviorEvent {

    public static final String TOPIC                = "BehaviorTrace";
    public static final String KEY_EXECUTION_ID     = "ExecutionId";
    public static final String KEY_ORIGINAL_DATA    = "OriginalData";
    public static final String KEY_DATA             = "Data";
    public static final String KEY_EX               = "Exception";
//    public static final String KEY_RESP_NAME         = "ResponsibleName";

    public BehaviorTraceEvent(String sourceName) {
        super(TOPIC, sourceName);
    }

    /**
     * Get this event associated behavior execution id.
     *
     * @return  The behavior execution id
     */
    public ExecutionIdentify executionId() {
        return (ExecutionIdentify) get(KEY_EXECUTION_ID);
    }

    /**
     * Get this event associated behavior name.
     *
     * @return  The behavior name
     */
    public String behaviorName() {
        return executionId().getName();
    }

    /**
     * Get original input data of this behavior
     *
     * @return  The original input data
     */
    public Object originalData() {
        return get(KEY_ORIGINAL_DATA);
    }

    /**
     * Get last outputted data of this behavior
     *
     * @return  The last outputted data
     */
    public Object data() {
        return get(KEY_DATA);
    }

    /**
     * Get cause object if the behavior execution failed.
     *
     * @return  The cause object or null
     */
    public Exception exception() {
        return (Exception) get(KEY_EX);
    }

//    public String responsibleName() {
//        return (String) get(KEY_RESP_NAME);
//    }
}
