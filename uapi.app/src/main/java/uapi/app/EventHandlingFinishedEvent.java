package uapi.app;

import uapi.behavior.BehaviorEvent;

/**
 * Created by xquan on 5/19/2017.
 */
public class EventHandlingFinishedEvent extends BehaviorEvent {

    public static final String TOPIC    = "EventHandlingFinished";

    public EventHandlingFinishedEvent(String sourceName) {
        super(TOPIC, sourceName);
    }
}
