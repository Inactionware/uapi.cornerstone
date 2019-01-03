/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.common.IAttributed;

import java.util.HashMap;
import java.util.Map;

public class ActionResult implements IAttributed {

    private final Map<Object, Object> _attributes;

    public ActionResult() {
        this._attributes = new HashMap<>();
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public boolean contains(Object key, Object value) {
        return false;
    }

    @Override
    public boolean contains(Map<Object, Object> attributes) {
        return false;
    }
}
