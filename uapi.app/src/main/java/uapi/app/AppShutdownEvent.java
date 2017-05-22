package uapi.app;

import uapi.behavior.BehaviorEvent;
import uapi.service.IService;

import java.util.List;

/**
 * An event for application shutdown
 */
public class AppShutdownEvent extends BehaviorEvent {

    public static final String TOPIC = "ApplicationShutdown";

    private final List<IService> _appSvcs;

    public AppShutdownEvent(
            final String sourceName,
            final List<IService> applicationServices
    ) {
        super(TOPIC, sourceName);
        this._appSvcs = applicationServices;
    }

    public List<IService> applicationServices() {
        return this._appSvcs;
    }
}
