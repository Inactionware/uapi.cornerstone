/*
 * Copyright (C) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;

/**
 * The meta class hold bass information for action input argument.
 */
public final class ActionInputMeta {

    private final Class<?> _type;

    public ActionInputMeta(
            final Class<?> type
    ) {
        ArgumentChecker.required(type, "type");
        this._type = type;
    }

    /**
     * Return type of Action input.
     *
     * @return  The type of Action input
     */
    public Class<?> type() {
        return this._type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof ActionInputMeta)) {
            return false;
        }
        var other = (ActionInputMeta) obj;
        return this._type.equals(other._type);
    }

    @Override
    public int hashCode() {
        return this._type.hashCode();
    }

    @Override
    public String toString() {
        return StringHelper.makeString("ActionInputMeta[type={}]", this._type.getCanonicalName());
    }
}
