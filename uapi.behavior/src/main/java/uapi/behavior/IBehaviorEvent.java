/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.event.IEvent;

/**
 * The event for Behavior only
 */
public interface IBehaviorEvent extends IEvent {

    /**
     * Attach a data for specific key
     *
     * @param   key
     *          The key
     * @param   data
     *          Attached data
     * @param   <T>
     *          The data type
     */
    <T> void attach(String key, T data);

    /**
     * Get attached data by specific key
     *
     * @param   key
     *          The key
     * @param   <T>
     *          The data type
     * @return  The data or null if no data was found
     */
    <T> T attachment(String key);

    /**
     * Clear data by specific key
     *
     * @param   key
     *          The key
     */
    void clearAttachment(String key);

    /**
     * Clear all data in this event
     */
    void clearAttachments();
}
