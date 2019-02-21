/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.event.AttributedEvent;

/**
 * The implementation for IBehaviorEvent interface
 */
public class BehaviorEvent extends AttributedEvent {

    public static final String KEY_SOURCE_NAME  = "SourceName";

    public BehaviorEvent(final String topic, final String sourceName) {
        super(topic);
        ArgumentChecker.required(sourceName, "sourceName");
        set(KEY_SOURCE_NAME, sourceName);
    }

    /**
     * Retrieve the source name which indicate which responsible fire this event
     *
     * @return  The name of responsible
     */
    public String sourceName() {
        return (String) get(KEY_SOURCE_NAME);
    }
}
