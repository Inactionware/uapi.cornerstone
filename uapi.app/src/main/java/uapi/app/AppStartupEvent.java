package uapi.app;

import uapi.behavior.BehaviorEvent;

/**
 * An event for application startup
 */
public class AppStartupEvent extends BehaviorEvent {

    public static final String TOPIC = "ApplicationStartup";

    public AppStartupEvent(String sourceName) {
        super(TOPIC, sourceName);
    }
}
