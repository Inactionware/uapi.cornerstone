/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.GeneralException;
import uapi.InvalidArgumentException;
import uapi.common.ArgumentChecker;

/**
 * The injection wrap an object which can be injected to other service.
 * The wrap contains all mata information about injected object.
 */
public class Injection {

    private final String _id;
    private final Object _object;

    public Injection(
            final String id,
            final Object object
    ) throws InvalidArgumentException {
        ArgumentChecker.notEmpty(id, "id");
        ArgumentChecker.notNull(object, "object");

        this._id = id;
        this._object = object;
    }

    public String getId() {
        return this._id;
    }

    public Object getObject() {
        return this._object;
    }

    public void checkType(
            final Class type
    ) throws GeneralException {
        if (! this._object.getClass().isAssignableFrom(type)) {
            throw new GeneralException(
                    "The object type {} can't match its type {}",
                    this._object.getClass(), type);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Injection[")
                .append("id=").append(this._id)
                .append(",object=").append(this._object);
        return sb.toString();
    }
}
