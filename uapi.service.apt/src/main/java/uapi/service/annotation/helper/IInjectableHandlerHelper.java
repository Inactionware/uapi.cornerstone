/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.annotation.helper;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.codegen.IHandlerHelper;

/**
 * A helper for maintain injectable annotation at build-time
 */
public interface IInjectableHandlerHelper extends IHandlerHelper {

    String name = "InjectableHelper";

    void addDependency(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String fieldName,
            final String fieldType,
            final String injectId,
            final String injectFrom,
            final boolean isCollection,
            final boolean isMap,
            final String mapKeyType,
            final boolean isOptional);
}
