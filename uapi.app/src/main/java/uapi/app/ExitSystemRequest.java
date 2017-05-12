package uapi.app;

import uapi.behavior.BehaviorEvent;

/**
 * The event is used to send exit system request
 */
public class ExitSystemRequest extends BehaviorEvent {

    public static final String TOPIC    = "ExitSystem";

    public ExitSystemRequest(final String sourceName) {
        super(TOPIC, sourceName);
    }
}
