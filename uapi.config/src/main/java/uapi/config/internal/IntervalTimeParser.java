/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import uapi.service.Tags;
import uapi.common.ArgumentChecker;
import uapi.common.CollectionHelper;
import uapi.common.IntervalTime;
import uapi.config.IConfigValueParser;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Value parse for {@code IntervalTime}
 */
@Service({ IConfigValueParser.class })
@Tag(Tags.CONFIG)
public class IntervalTimeParser implements IConfigValueParser<IntervalTime> {

    private static final String[] supportTypesIn = new String[] { String.class.getCanonicalName() };
    private static final String[] supportTypesOut = new String[] { IntervalTime.class.getCanonicalName() };

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportTypesIn, inType) && CollectionHelper.isContains(supportTypesOut, outType);
    }

    @Override
    public String getName() {
        return IntervalTimeParser.class.getCanonicalName();
    }

    @Override
    public IntervalTime parse(Object value) {
        ArgumentChecker.required(value, "value");
        return IntervalTime.parse(value.toString());
    }
}

