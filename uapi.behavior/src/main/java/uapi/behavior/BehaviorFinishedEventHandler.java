package uapi.behavior;

/**
 * A handler to handle BehaviorFinishedEvent
 */
@FunctionalInterface
public interface BehaviorFinishedEventHandler {

    /**
     * Handle BehaviorFinishedEvent
     *
     * @param   event
     *          The BehaviorFinishedEvent
     */
    void accept(BehaviorFinishedEvent event);
}
