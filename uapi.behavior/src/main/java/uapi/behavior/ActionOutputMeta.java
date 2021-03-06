/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;

/**
 * The meta class holds basic information for action output argument.
 */
public final class ActionOutputMeta {

    private final Class<?> _type;
    private final String _name;

    /**
     * Create anonymous action output meta, it only used system internally
     *
     * @param   type
     *          Action output type
     */
    public ActionOutputMeta(
            final Class<?> type
    ) {
        this(type, null);
    }

    public ActionOutputMeta(
            final Class<?> type,
            final String name
    ) {
        ArgumentChecker.required(type, "type");
        this._type = type;
        this._name = name;
    }

    /**
     * Return type of Action input argument.
     *
     * @return  The type of Action input argument
     */
    public Class<?> type() {
        return this._type;
    }

    /**
     * Return name of Action output argument, the name is used to receive Action output from behavior context.
     *
     * @return  The name of Action output argument
     */
    public String name() {
        return this._name;
    }

    public boolean isAnonymous() {
        return this._name == null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (! (other instanceof ActionOutputMeta)) {
            return false;
        }
        var otherMeta = (ActionOutputMeta) other;
        return this._name.equals(otherMeta._name) && this._type.equals(otherMeta._type);
    }

    @Override
    public String toString() {
        return StringHelper.makeString("ActionOutputMeta[name={}, type={}]", this._name, this._type);
    }
}
