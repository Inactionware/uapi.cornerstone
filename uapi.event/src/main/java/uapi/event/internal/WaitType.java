package uapi.event.internal;

/**
 * The enumeration list how event bus's behavior after an event was sent
 */
public enum WaitType {

    /**
     * Do not wait, return immediately
     */
    NO_WAIT,

    /**
     * Block sender thread unit the event is handled
     */
    BLOCKED,

    /**
     * Invoke a callback when the event is handled
     */
    CALLBACK;
}
