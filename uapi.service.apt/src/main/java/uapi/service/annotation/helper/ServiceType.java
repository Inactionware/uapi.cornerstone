/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.annotation.helper;

/**
 * Service type
 */
public enum ServiceType {

    /**
     * If the service is tagged as Singleton which means framework will create only
     * on instance for this service at application lifecycle.
     */
    Singleton,

    /**
     * If the service is tagged as Prototype which means framework will create new
     * instance from the prototype service when other service reference the prototype
     * service.
     */
    Prototype
}
