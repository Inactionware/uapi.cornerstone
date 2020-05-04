/*
 * Copyright (c) 2020. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.service.annotation.helper.ServiceType;

/**
 * Hold meta data for Action
 */
public interface IActionMeta {

    /**
     * Return the identify of the action
     *
     * @return  The action identify
     */
    ActionIdentify actionId();

    /**
     * Return the action service type
     *
     * @return  The action service type
     */
    default ServiceType serviceType() {
        return ServiceType.Singleton;
    };
}
