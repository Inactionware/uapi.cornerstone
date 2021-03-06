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
 * A helper for maintain service annotation at build-time
 */
public interface IServiceHandlerHelper extends IHandlerHelper {

    String name = "ServiceHelper";

    void addServiceId(ClassMeta.Builder classBuilder, String... serviceIds);

    void becomeService(IBuilderContext builderCtx, ClassMeta.Builder classBuilder, String... serviceIds);
}
