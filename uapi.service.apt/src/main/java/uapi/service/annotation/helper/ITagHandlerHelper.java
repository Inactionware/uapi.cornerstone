/*
 * Copyright (c) 2020. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.service.annotation.helper;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.codegen.IHandlerHelper;

public interface ITagHandlerHelper extends IHandlerHelper {

    public static final String name = "TagHelper";

    @Override
    default String getName() {
        return name;
    }

    void setTags(IBuilderContext builderCtx, ClassMeta.Builder classBuilder, String... tag);
}
