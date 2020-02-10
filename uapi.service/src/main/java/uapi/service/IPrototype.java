/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import java.util.Map;

/**
 * A prototype can create new instance from its self
 */
public interface IPrototype extends IService {

    /**
     * Retrieve attributes which is required to create new instance from prototype.
     *
     * @return  The attribute list
     */
    String[] attributes();

    /**
     * Create new instance from the prototype
     *
     * @param   attributes
     *          The attribute which is used in new instance
     * @return  The created instance
     */
    IInstance newInstance(Map<Object, Object> attributes);
}
