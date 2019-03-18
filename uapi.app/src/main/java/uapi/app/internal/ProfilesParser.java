/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal;

import uapi.service.Tags;
import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.config.IConfigValueParser;
import uapi.common.CollectionHelper;
import uapi.rx.Looper;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse profile configuration list to profile
 */
@Service(IConfigValueParser.class)
@Tag(Tags.PROFILE)
public class ProfilesParser implements IConfigValueParser {

    private static final String NAME        = "name";
    private static final String MODEL       = "model";
    private static final String MATCHING    = "matching";
    private static final String TAGS        = "tags";

    private static final String[] supportedTypesIn = new String[] {
            List.class.getCanonicalName()
    };
    private static final String[] supportedTypesOut = new String[] {
            Map.class.getCanonicalName()
    };

    @Override
    public String getName() {
        return ProfilesParser.class.getName();
    }

    @Override
    public boolean isSupport(String inType, String outType) {
        return CollectionHelper.isContains(supportedTypesIn, inType) && CollectionHelper.isContains(supportedTypesOut, outType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Profile> parse(Object value) {
        List<Map> profileList = (List<Map>) value;
        Map<String, Profile> profiles = new HashMap<>();
        Looper.on(profileList)
                .foreach(profileCfg -> {
                    String name = profileCfg.get(NAME).toString();
                    if (profiles.containsKey(name)) {
                        throw AppException.builder()
                                .errorCode(AppErrors.DUPLICATED_PROFILE)
                                .variables(new AppErrors.DuplicatedProfile()
                                        .name(name))
                                .build();
                    }
                    Profile.Model model = Profile.Model.parse(profileCfg.get(MODEL).toString());
                    Profile.Matching matching = Profile.Matching.parse(profileCfg.get(MATCHING).toString());
                    Object tagsObj = profileCfg.get(TAGS);
                    if (! (tagsObj instanceof List)) {
                        throw AppException.builder()
                                .errorCode(AppErrors.TAG_CONFIG_IS_NOT_LIST)
                                .build();
                    }
                    List<String> tagList = (List<String>) tagsObj;
                    String[] tags = tagList.toArray(new String[tagList.size()]);
                    Profile profile = new Profile(name, model, matching, tags);
                    profiles.put(name, profile);
                });
        return profiles;
    }
}
