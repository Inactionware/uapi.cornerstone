/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import java.util.Map;

/**
 * Store context during execution time
 */
public interface IExecutionContext {

    String KEY_RESP_NAME    = "ResponsibleName";
    String KEY_BEHA_NAME    = "BehaviorName";
    String KEY_ORI_EVENT    = "OriginalEvent";
    String KEY_BEHA_INPUTS  = "BehaviorInputs";

    /**
     * Put single k/v data under behavior scope
     *
     * @param   key
     *          The data key
     * @param   value
     *          The data value
     */
    void put(Object key, Object value);

    /**
     * Put single k/v data under specific scope
     *
     * @param   key
     *          The data key
     * @param   value
     *          The data value
     * @param   scope
     *          The scope
     */
    void put(Object key, Object value, Scope scope);

    /**
     * Put multiple k/v data under specific scope
     *
     * @param   data
     *          The k/v data
     * @param   scope
     *          The scope
     */
    void put(Map data, Scope scope);

    /**
     * Receive data by specific key
     *
     * @param   key
     *          The key
     * @return  The value
     */
    <T> T get(Object key);

    default String responsibleName() {
        return get(KEY_RESP_NAME);
    }

    default String behaviorName() {
        return get(KEY_BEHA_NAME);
    }

    @SuppressWarnings("unchecked")
    default <T extends BehaviorEvent> T originalEvent() {
        return (T) get(KEY_ORI_EVENT);
    }

    default Object[] behaviorInputs() {
        return (Object[]) get(KEY_BEHA_INPUTS);
    }

    /**
     * Fire event by synchronized way
     *
     * @param   event
     *          Fired event
     */
    void fireEvent(BehaviorEvent event);

    /**
     * Fire event by synchronized or asynchronous way
     *
     * @param   event
     *          The fired event
     * @param   sync
     *          True is synchronized otherwise asynchronous
     */
    void fireEvent(BehaviorEvent event, boolean sync);
}
