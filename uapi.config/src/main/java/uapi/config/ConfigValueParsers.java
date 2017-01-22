/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config;

import uapi.GeneralException;
import uapi.common.ArgumentChecker;
import uapi.rx.Looper;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * The service hold one or more than one parsers
 */
@Service
//@Tag("Config")
public class ConfigValueParsers {

//    @Inject
    protected List<IConfigValueParser> _parsers = new ArrayList<>();

    public IConfigValueParser findParser(String inType, String outType) {
        ArgumentChecker.notEmpty(inType, "inType");
        ArgumentChecker.notEmpty(outType, "outType");
        List<IConfigValueParser> matcheds = Looper.on(this._parsers)
                .filter(parser -> parser.isSupport(inType, outType))
                .toList();
        if (matcheds == null || matcheds.size() == 0) {
            throw new GeneralException("No parser for in type {} and out type {}",
                    inType, outType);
        }
        if (matcheds.size() > 1) {
            throw new GeneralException("Found more than one parser for in type {} and out type: {}",
                    inType, outType, matcheds);
        }
        return matcheds.get(0);
    }

    public IConfigValueParser findParser(String name) {
        ArgumentChecker.notEmpty(name, "name");
        List<IConfigValueParser> matches = Looper.on(this._parsers)
                .filter(parser -> parser.getName().equals(name))
                .toList();
        if (matches == null || matches.size() == 0) {
            throw new GeneralException("No parser with name {}", name);
        }
        if (matches.size() > 1) {
            throw new GeneralException("Found more than one parser with name {} : {}",
                    name, matches);
        }
        return matches.get(0);
    }
}
