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
import uapi.event.PlainEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for IBehaviorEvent interface
 */
public class BehaviorEvent extends PlainEvent implements IBehaviorEvent {

    private final Map<String, Object> _attachments;

    public BehaviorEvent(final String topic) {
        super(topic);
        this._attachments = new HashMap<>();
    }

    @Override
    public <T> void attach(String key, T data) {
        ArgumentChecker.required(key, "key");
        ArgumentChecker.required(data, "data");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T attachment(String key) {
        ArgumentChecker.required(key, "key");
        return (T) this._attachments.get(key);
    }

    @Override
    public void clearAttachment(String key) {
        ArgumentChecker.required(key, "key");
        this._attachments.remove(key);
    }

    @Override
    public void clearAttachments() {
        this._attachments.clear();
    }
}
