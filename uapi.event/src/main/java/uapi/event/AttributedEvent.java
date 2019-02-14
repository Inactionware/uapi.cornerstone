package uapi.event;

import uapi.common.ArgumentChecker;
import uapi.common.IAttributed;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * An attributed event has one or more attributes associated with it
 */
public class AttributedEvent extends PlainEvent implements IAttributed {

    private final Map<Object, Object> _attributes;

    public AttributedEvent(String topic) {
        super(topic);
        this._attributes = new HashMap<>();
    }

    public Object set(Object key, Object attribute) {
        ArgumentChecker.required(key, "key");
        return this._attributes.put(key, attribute);
    }

    @Override
    public Object get(Object key) {
        ArgumentChecker.required(key, "key");
        return this._attributes.get(key);
    }

    @Override
    public boolean contains(Object key, Object value) {
        Object thisValue = this._attributes.get(key);
        if (thisValue == null) {
            return false;
        }
        return thisValue.equals(value);
    }

    @Override
    public boolean contains(Map<Object, Object> map) {
        if (map == null || map.size() == 0) {
            return true;
        }
        int matchedCount = Looper.on(map.entrySet())
                .filter(entry -> this._attributes.containsKey(entry.getKey()))
                .filter(entry -> entry.getValue().equals(this._attributes.get(entry.getKey())))
                .count();
        return matchedCount == map.size();
    }

    @Override
    public int count() {
        return this._attributes.size();
    }
}
