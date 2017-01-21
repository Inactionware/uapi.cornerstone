/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

/**
 * Created by xquan on 12/2/2016.
 */
enum ServiceState {
    Unresolved(0),
    Resolved(10),
    Injected(20),
    Satisfied(30),
    Activated(40),
    Deactivated(50),
    Destroyed(-1);

    private int _value;

    int value() {
        return this._value;
    }

    ServiceState(int value) {
        this._value = value;
    }
}