package uapi.event;

import java.util.Map;

/**
 * The handler is used to handle event which has matched event's attributes
 */
public interface IAttributedEventHandler<T extends AttributedEvent>
        extends IEventHandler<T> {

    /**
     * Get attributes which should be matched event's attributes
     *
     * @return  The attributes
     */
    Map<Object, Object> getAttributes();
}
