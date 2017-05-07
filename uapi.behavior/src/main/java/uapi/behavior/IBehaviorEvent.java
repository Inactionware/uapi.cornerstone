package uapi.behavior;

import uapi.behavior.internal.Responsible;

/**
 * Created by min on 2017/5/7.
 */
public interface IBehaviorEvent {

    Responsible getSource();
}
