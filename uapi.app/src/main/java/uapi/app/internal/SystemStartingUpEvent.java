package uapi.app.internal;

import uapi.event.IEvent;
import uapi.service.IService;

import java.util.List;

/**
 * The event indicate that the system is launched
 */
public class SystemStartingUpEvent implements IEvent {

    public static final String TOPIC = SystemStartingUpEvent.class.getCanonicalName();

    private final long _startTime;
    private final List<IService> _appSvcs;

    public SystemStartingUpEvent(
            final long startTime,
            final List<IService> applicationServices
    ) {
        this._startTime = startTime;
        this._appSvcs = applicationServices;
    }

    @Override
    public String topic() {
        return TOPIC;
    }

    public long startTime() {
        return this._startTime;
    }

    public List<IService> applicationServices() {
        return this._appSvcs;
    }
}
