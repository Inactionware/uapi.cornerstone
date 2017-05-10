package uapi.app.internal;

import uapi.behavior.BehaviorEvent;

/**
 * The even indicate the system is shutting down
 */
public class SystemShuttingDownEvent extends BehaviorEvent {

    public static final String SOURCE_NAME  = "_SYSTEM_";
    public static final String TOPIC        = SystemShuttingDownEvent.class.getCanonicalName();

    private final Throwable _cause;

    public SystemShuttingDownEvent(final Throwable cause) {
        super(TOPIC, SOURCE_NAME);
        this._cause = cause;
    }

    public Throwable cause() {
        return this._cause;
    }
}
