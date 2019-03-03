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

public final class ActionInputReference {

    private static final int NO_INDEX   = -1;
    private static final String SEP     = ".";

    public static final String generateKey(
            final String label,
            final String name
    ) {
        return generateKey(label, name, NO_INDEX);
    }

    public static final String generateKey(
            final String label,
            final String name,
            final int index
    ) {
        verify(label);
        verify(name);
        if (index > NO_INDEX) {
            return StringHelper.makeString("{}{}{}{}{}", label, SEP, name, SEP, index);
        } else {
            return StringHelper.makeString("{}{}{}", label, SEP, name);
        }
    }

    private final String _label;
    private final String _name;
    private final int _idx;

    public ActionInputReference(
            final String label,
            final String name
    ) {
        this(label, name, NO_INDEX);
    }

    public ActionInputReference(
            final String label,
            final String name,
            final int index
    ) {
        verify(label);
        verify(name);
        this._label = label;
        this._name = name;
        this._idx = index;
    }

    public String label() {
        return this._label;
    }

    public String name() {
        return this._name;
    }

    public String toKey() {
        return generateKey(this._label, this._name, this._idx);
    }

    /**
     * Verify specific string has invalid char or not
     *
     * @param   str
     *          The string
     */
    private static void verify(final String str) {
        ArgumentChecker.required(str, "str");
        ArgumentChecker.notContains(str, "str", SEP);
    }
}
