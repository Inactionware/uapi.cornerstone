/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.IIdentifiable;

/**
 * A service loader used to load external service
 */
public interface IServiceLoader extends Comparable<IServiceLoader>, IIdentifiable<String> {

    /**
     * Compare two service load based on its priority
     *
     * @param   other Other service loader instance
     * @return  priority
     */
    default int compareTo(IServiceLoader other) {
        if (getPriority() < other.getPriority()) {
            return -1;
        }
        if (getPriority() > other.getPriority()) {
            return 1;
        }
        return 0;
    }

    /**
     * Get priority of this service loader.
     * Small value has high priority.
     *
     * @return  The priority value of this service loader
     */
    int getPriority();

    /**
     * Load service by id and type
     * If the service is null then the service load must be listen on the service, if the service is ready then the
     * ServiceReadyListener must be invoked
     *
     * @param   serviceId
     *          The service id
     * @param   serviceType
     *          The service type
     * @param   <T>
     *          The service instance type
     * @return  The service instance or null
     */
    <T> T load(final String serviceId, final Class<?> serviceType);

    /**
     * Register service ready listener
     * The listener will be invoked when specific service is ready to use
     * @param listener
     */
    void register(IServiceReadyListener listener);

    /**
     * The listener is used to notify when specific service is ready to use
     */
    interface IServiceReadyListener {

        /**
         * The method is invoked when specific service is ready to use
         *
         * @param   dependency
         *          The dependency
         *
         * @param   service
         *          The service instance
         */
        void onReady(Dependency dependency, Object service);
    }
}
