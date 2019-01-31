package uapi.behavior;

/**
 * The clss contains methods which should be used for trace behavior execution
 */
public abstract class BehaviorTraceEvent extends BehaviorEvent {

    public static final String TOPIC                = "BehaviorTrace";
    public static final String KEY_EXECUTION_ID     = "ExecutionId";
    public static final String KEY_BEHAVIOR_INPUTS  = "BehaviorInputs";
    public static final String KEY_CURRENT_OUTPUTS  = "CurrentOutputs";
    public static final String KEY_CURRENT_RESULT   = "CurrentResult";
    public static final String KEY_EX               = "Exception";

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
    public Object[] behaviorInputs() {
        return (Object[]) get(KEY_BEHAVIOR_INPUTS);
    }

    /**
     * Get outputs by current action execution
     *
     * @return  The current action outputs
     */
    public ActionOutput[] currentOutputs() {
        return (ActionOutput[]) get(KEY_CURRENT_OUTPUTS);
    }

    /**
     * Get last outputted data of this behavior
     *
     * @return  The last outputted data
     */
    public ActionResult currentResult() {
        return (ActionResult) get(KEY_CURRENT_RESULT);
    }

    /**
     * Get cause object if the behavior execution failed.
     *
     * @return  The cause object or null
     */
    public Exception exception() {
        return (Exception) get(KEY_EX);
    }
}
