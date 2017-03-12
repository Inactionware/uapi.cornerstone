/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

/**
 * A lifecycle management for service
 */
public interface IServiceLifecycle {

    /**
     * Invoked when a injectable property is set
     *
     * @param   serviceId
     *          The injected service id
     * @param   service
     *          The injected service
     */
    void onServiceInjected(String serviceId, Object service);

    /**
     * Invoked when the service is resolved and all other conditions is satisfied.
     * Resolved means all dependencies has been set
     * Satisfied means that like configurations has been set
     */
    void onInit();
}
