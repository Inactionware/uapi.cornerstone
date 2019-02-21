package uapi.behavior;

/**
 * Any action implements this interface means the action can be intercepted by other action (interceptor)
 */
public interface IIntercepted {

    /**
     * The action id which can intercept this action
     *
     * @return  The action id
     */
    ActionIdentify[] by();
}
