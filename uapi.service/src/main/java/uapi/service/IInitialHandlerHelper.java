/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.codegen.IHandlerHelper;

/**
 * The handler helper is used to help add specific method to the init method
 */
public interface IInitialHandlerHelper extends IHandlerHelper {

    String name = "InitialHelper";

    /**
     * Add new method to the init method.
     * The method must be no arguments and return nothing
     *
     * @param   builderContext
     *          The context of compiling time
     * @param   classBuilder
     *          The builder for the class
     * @param   methodNames
     *          The method name
     */
    void addInitMethod(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String target,
            final String... methodNames);
}
