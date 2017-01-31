/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

/**
 * The lifecycle for the data which is put in the execution context.
 */
public enum Scope {

    /**
     * Always keep the data until the context is destroyed
     */
    GLOBAL,

    /**
     * Keep the data only when the context go throw same behavior
     */
    BEHAVIOR
}
