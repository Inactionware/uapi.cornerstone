/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import uapi.service.Tags;
import uapi.common.ArgumentChecker;
import uapi.common.Capacity;
import uapi.common.CollectionHelper;
import uapi.config.IConfigValueParser;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

@Service({ IConfigValueParser.class })
@Tag(Tags.CONFIG)
public class CapacityParser implements IConfigValueParser {

    private static final String[] supportTypesIn = new String[] { String.class.getCanonicalName() };
    private static final String[] supportTypesOut = new String[] { Capacity.class.getCanonicalName() };

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportTypesIn, inType) && CollectionHelper.isContains(supportTypesOut, outType);
    }

    @Override
    public String getName() {
        return CapacityParser.class.getCanonicalName();
    }

    @Override
    public Capacity parse(Object value) {
        ArgumentChecker.required(value, "value");
        return Capacity.parse(value.toString());
    }
}
