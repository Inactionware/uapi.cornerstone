package uapi.app;

import uapi.behavior.BehaviorEvent;
import uapi.service.IService;

import java.util.List;

/**
 * An event for application shutdown
 */
public class AppShutdownEvent extends BehaviorEvent {

    public static final String TOPIC = "ApplicationShutdown";

    public AppShutdownEvent(final String sourceName) {
        super(TOPIC, sourceName);
    }
}
