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

    void fireEvent(IBehaviorTraceEvent event);

    void fireEvent(BehaviorEvent event);
}
