/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import uapi.Tags;
import uapi.Type;
import uapi.common.CollectionHelper;
import uapi.config.IConfigValueParser;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * The parser used to parse config value which can be convert to Integer
 */
@Service({ IConfigValueParser.class })
@Tag(Tags.CONFIG)
public class BooleanValueParser implements IConfigValueParser {

    private static final String[] supportTypesIn  = new String[] {
            Type.Q_STRING, Type.BOOLEAN, Type.Q_BOOLEAN };
    private static final String[] supportTypesOut = new String[] {
            Type.BOOLEAN, Type.Q_BOOLEAN };

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportTypesIn, inType) && CollectionHelper.isContains(supportTypesOut, outType);
    }

    @Override
    public String getName() {
        return BooleanValueParser.class.getCanonicalName();
    }

    @Override
    public Boolean parse(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
