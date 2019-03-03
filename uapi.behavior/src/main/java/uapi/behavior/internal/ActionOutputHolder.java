/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.behavior.ActionInputReference;
import uapi.behavior.ActionOutputMeta;
import uapi.rx.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * A class is used to holder action output data
 */
public class ActionOutputHolder {

    private final ActionOutputMeta[] _outMetas;
    private final Map<String, Object> _namedDatas;
    private final Object[] _outDatas;

    ActionOutputHolder(
            final ActionOutputMeta[] outputMetas,
            final Object[] outputDatas
    ) {
        this._outMetas = outputMetas;
        this._outDatas = outputDatas;
        this._namedDatas = new HashMap<>();
        Looper.on(outputMetas).foreachWithIndex((idx, meta) -> {
            if (! meta.isAnonymous()) {
                this._namedDatas.put(meta.name(), outputDatas[idx]);
            }
        });
    }

    Object getData(final ActionInputReference inRef) {
        Object data = null;
        if (inRef.name() != null) {
            data = this._namedDatas.get(inRef.name());
        }
        if (data != null) {
            return data;
        }
        return data;
    }
}
