/**
 * Copyright (C) 2010 The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

//import uapi.Type;

import uapi.Type;

/**
 * Implement this interface will indicate the object is managed by framework
 */
public interface IService {

    String METHOD_GETIDS                        = "getIds";
    String METHOD_GET_DEPENDENCIES              = "getDependencies";
    String METHOD_AUTOACTIVE                    = "autoActive";
    String METHOD_GETIDS_RETURN_TYPE            = Type.STRING_ARRAY;
    String METHOD_GET_DEPENDENT_ID_RETURN_TYPE  = Type.STRING_ARRAY;
    String METHOD_AUTOACTIVE_RETURN_TYPE        = Type.BOOLEAN;

    /**
     * Return the service identifications
     *
     * @return  The service identifications
     */
    String[] getIds();

    /**
     * Indicate the service should be activated after system launched automatically or not
     *
     * @return  True means activated the service otherwise do nothing
     */
    boolean autoActive();
}