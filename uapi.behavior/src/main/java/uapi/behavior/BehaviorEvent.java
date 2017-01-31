/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.event.AttributedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for IBehaviorEvent interface
 */
public class BehaviorEvent extends AttributedEvent {

    private final Map<String, Object> _attachments;

    public BehaviorEvent(final String topic) {
        super(topic);
        this._attachments = new HashMap<>();
    }
}
