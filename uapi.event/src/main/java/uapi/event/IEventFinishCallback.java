package uapi.event;

/**
 * Created by xquan on 5/18/2017.
 */
@FunctionalInterface
public interface IEventFinishCallback<T extends IEvent> {

    /**
     * Invoked when specific event handling is finished.
     *
     * @param   event
     *          Finished event
     */
    void callback(T event);
}
