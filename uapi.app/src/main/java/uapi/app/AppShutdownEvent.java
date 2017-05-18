package uapi.app;

import uapi.behavior.BehaviorEvent;

/**
 * An event for application shutdown
 */
public class AppShutdownEvent extends BehaviorEvent {

    public static final String TOPIC = "ApplicationShutdown";

    public AppShutdownEvent(String sourceName) {
        super(TOPIC, sourceName);
    }
}
