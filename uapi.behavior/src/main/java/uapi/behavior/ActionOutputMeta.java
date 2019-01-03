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

/**
 * The meta class holds basic information for action output argument.
 */
public final class ActionOutputMeta {

    private final Class<?> _type;
    private final String _name;

    public ActionOutputMeta(
            final Class<?> type,
            final String name
    ) {
        ArgumentChecker.required(type, "type");
        ArgumentChecker.required(name, "name");
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
}
