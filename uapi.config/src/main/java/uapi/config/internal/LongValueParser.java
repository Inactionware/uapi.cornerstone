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
import uapi.common.CollectionHelper;
import uapi.config.IConfigValueParser;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * The parser used to parse config value which can be convert to Double
 */
@Service(IConfigValueParser.class)
@Tag(Tags.CONFIG)
public class LongValueParser implements IConfigValueParser<Long> {

    private static final String[] supportTypesIn = new String[] {
            Long.class.getCanonicalName(), String.class.getCanonicalName() };
    private static final String[] supportTypesOut = new String[] {
            "long", Long.class.getCanonicalName()
    };

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportTypesIn, inType) && CollectionHelper.isContains(supportTypesOut, outType);
    }

    @Override
    public String getName() {
        return LongValueParser.class.getCanonicalName();
    }

    @Override
    public Long parse(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        return Long.parseLong(value.toString());
    }
}
