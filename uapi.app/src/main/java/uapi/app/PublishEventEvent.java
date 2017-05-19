package uapi.app;

import uapi.behavior.BehaviorEvent;
import uapi.event.IEvent;

/**
 * Created by xquan on 5/19/2017.
 */
public class PublishEventEvent extends BehaviorEvent {

    public static final String TOPIC    = "PublishEvent";

    private final IEvent _event;
    private final boolean _needNotify;

    public PublishEventEvent(
            final String sourceName,
            final IEvent publishEvent
    ) {
        this(sourceName, publishEvent, false);
    }

    public PublishEventEvent(
            final String sourceName,
            final IEvent publishEvent,
            final boolean needNotify
    ) {
        super(TOPIC, sourceName);
        this._event = publishEvent;
        this._needNotify = needNotify;
    }

    public IEvent event() {
        return this._event;
    }

    public boolean needNotify() {
        return this._needNotify;
    }
}
