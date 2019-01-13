/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.common.StringHelper;

public final class ActionInputReference {

    public static final String generateKey(
            final String label,
            final String name
    ) {
        return StringHelper.makeString("{}.{}", label, name);
    }

    private final String _label;
    private final String _name;

    public ActionInputReference(
            final String label,
            final String name
    ) {
        this._label = label;
        this._name = name;
    }

    public String label() {
        return this._label;
    }

    public String name() {
        return this._name;
    }

    public String toKey() {
        return generateKey(this._label, this._name);
    }
}
