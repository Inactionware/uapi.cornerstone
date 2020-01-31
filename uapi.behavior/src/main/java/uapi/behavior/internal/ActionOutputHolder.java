/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.behavior.ActionOutputMeta;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * A class is used to holder action output data
 */
class ActionOutputHolder {

    private final ActionOutputMeta[] _outMetas;
    private final Map<String, Object> _namedDatas;
    private final Object[] _outData;

    ActionOutputHolder(
            final ActionOutputMeta[] outputMetas,
            final Object[] outputData
    ) {
        this._outMetas = outputMetas;
        this._outData = outputData;
        this._namedDatas = new HashMap<>();
        Looper.on(outputMetas).foreachWithIndex((idx, meta) -> {
            if (! meta.isAnonymous()) {
                this._namedDatas.put(meta.name(), outputData[idx]);
            }
        });
    }

    Object[] getData() {
        return this._outData;
    }

    Object getData(final String name) {
        return this._namedDatas.get(name);
    }

    Object getData(final int index) {
        return this._outData[index];
    }
}
