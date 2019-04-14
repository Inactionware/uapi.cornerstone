package uapi.app.internal;

import uapi.behavior.BehaviorEvent;
import uapi.service.IService;

import java.util.List;

/**
 * The event indicate that the system is launched
 */
public class SystemStartingUpEvent extends BehaviorEvent {

    public static final String SOURCE_NAME  = "_SYSTEM_";
    public static final String TOPIC        = SystemStartingUpEvent.class.getCanonicalName();

    private final long _startTime;
    private final List<IService> _appSvcs;

    public SystemStartingUpEvent(
            final long startTime,
            final List<IService> applicationServices
    ) {
        super(TOPIC, SOURCE_NAME);
        this._startTime = startTime;
        this._appSvcs = applicationServices;
    }

    public long startTime() {
        return this._startTime;
    }

    public List<IService> applicationServices() {
        return this._appSvcs;
    }
}
