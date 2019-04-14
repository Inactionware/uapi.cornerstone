package uapi.app.internal;

import uapi.behavior.BehaviorEvent;
import uapi.service.IService;

import java.util.List;

/**
 * The even indicate the system is shutting down
 */
public class SystemShuttingDownEvent extends BehaviorEvent {

    public static final String SOURCE_NAME  = "_SYSTEM_";
    public static final String TOPIC        = SystemShuttingDownEvent.class.getCanonicalName();

    private final List<IService> _appSvcs;
    private final Throwable _cause;

    public SystemShuttingDownEvent(
            final List<IService> applicationServices,
            final Throwable cause
    ) {
        super(TOPIC, SOURCE_NAME);
        this._appSvcs = applicationServices;
        this._cause = cause;
    }

    public List<IService> applicationServices() {
        return this._appSvcs;
    }

    public Throwable cause() {
        return this._cause;
    }
}
