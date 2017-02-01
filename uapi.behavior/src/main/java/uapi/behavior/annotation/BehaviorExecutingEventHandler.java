package uapi.behavior.annotation;

import uapi.behavior.BehaviorExecutingEvent;

/**
 * Created by min on 2017/2/1.
 */
@FunctionalInterface
public interface BehaviorExecutingEventHandler {

    void accept(BehaviorExecutingEvent event);
}
