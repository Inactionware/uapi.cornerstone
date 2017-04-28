package uapi.app.internal;

import uapi.event.IEvent;

/**
 * The even indicate the system is shutting down
 */
public class SystemShuttingDownEvent implements IEvent {

    public static final String TOPIC = SystemShuttingDownEvent.class.getCanonicalName();

    private final Throwable _cause;

    public SystemShuttingDownEvent(final Throwable cause) {
        this._cause = cause;
    }

    @Override
    public String topic() {
        return TOPIC;
    }

    public Throwable cause() {
        return this._cause;
    }
}
