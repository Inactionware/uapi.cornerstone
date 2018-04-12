/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.rx.Looper;

import java.util.Map;

public interface IInstance extends IService {

    String METHOD_PROTOTYPE_ID  = "prototypeId";
    String METHOD_ATTRIBUTES    = "attributes";

    Map<String, ?> attributes();

    String prototypeId();

    @Override
    default String[] getIds() {
        StringBuilder buffer = new StringBuilder();
        Looper.on(attributes().entrySet())
                .map(Map.Entry::getValue)
                .map(Object::toString)
                .foreach(str -> buffer.append("_").append(str));
        return new String[] { prototypeId() + buffer.toString() };
    }

    @Override
    default boolean autoActive() {
        return false;
    }
}
