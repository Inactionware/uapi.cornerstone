/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.IBehaviorBuilder;
import uapi.behavior.IResponsible;
import uapi.common.ArgumentChecker;

/**
 * Created by xquan on 10/11/2016.
 */
public class Responsible implements IResponsible {

    private String _name;

    public void setName(String name) {
        ArgumentChecker.required(name, "name");
        this._name = name;
    }

    @Override
    public String name() {
        return this._name;
    }

    @Override
    public IBehaviorBuilder on(String topic) {
        return null;
    }

    @Override
    public IBehaviorBuilder on(Class<?> type) {
        return null;
    }

}
