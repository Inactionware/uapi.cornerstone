/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.InvalidArgumentException;
import uapi.behavior.ActionIdentify;
import uapi.behavior.ActionType;
import uapi.common.ArgumentChecker;

/**
 * An identify for behavior execution
 */
public class ExecutionIdentify extends ActionIdentify {

    private final int _sequence;
    private final String _id;

    public ExecutionIdentify(
            final ActionIdentify actionId,
            final int sequence) {
        this(actionId.getName(), actionId.getType(), sequence);
    }

    public ExecutionIdentify(
            final String name,
            final ActionType type,
            final int sequence
    ) {
        super(name, type);
        if (type != ActionType.BEHAVIOR) {
            throw new InvalidArgumentException(
                    "Expect action type is BEHAVIOR but found - {}", type);
        }
        this._sequence = sequence;
        this._id = super.getId() + SEPARATOR + sequence;
    }

    public int getSequence() {
        return this._sequence;
    }

    @Override
    public String getId() {
        return this._id;
    }

    @Override
    public Object[] getParts() {
        return new Object[] { getName(), getType(), getSequence() };
    }
}
