package uapi.behavior;

/**
 * A handler used to handle BehaviorExecutingEvent
 */
@FunctionalInterface
public interface BehaviorExecutingEventHandler {

    /**
     * Handle BehaviorExecutingEvent
     *
     * @param   event
     *          The BehaviorExecutingEvent
     */
    void accept(BehaviorExecutingEvent event);
}
