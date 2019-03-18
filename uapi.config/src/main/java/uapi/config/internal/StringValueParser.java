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
 * The parser used to parse config value which can be convert to String
 */
@Service({ IConfigValueParser.class })
@Tag(Tags.CONFIG)
public class StringValueParser implements IConfigValueParser {

    private static final String[] supportedInTypes  = new String[] {
            String.class.getCanonicalName() };
    private static final String[] supportedOutTypes = new String[] {
            String.class.getCanonicalName() };

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportedInTypes, inType) && CollectionHelper.isContains(supportedOutTypes, outType);
    }

    @Override
    public String getName() {
        return StringValueParser.class.getCanonicalName();
    }

    @Override
    public String parse(Object value) {
        return value.toString();
    }
}
