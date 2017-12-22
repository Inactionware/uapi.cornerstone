/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal;

import uapi.GeneralException;
import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.common.ArgumentChecker;
import uapi.common.CollectionHelper;
import uapi.service.IService;
import uapi.service.ITagged;

/**
 * A profile implementation
 */
public class Profile implements IProfile {

    private String _name;
    private String[] _tags;
    private Model _model;
    private Matching _matching;

    Profile(String name, Model model, Matching matching, String[] tags) {
        ArgumentChecker.notEmpty(name, "name");
        ArgumentChecker.required(model, "model");
        ArgumentChecker.required(matching, "matching");
        ArgumentChecker.required(tags, "tags");

        this._name = name;
        this._model = model;
        this._matching = matching;
        this._tags = tags;
    }

    public String getName() {
        return this._name;
    }

    public Model getModel() {
        return this._model;
    }

    public Matching getMatching() {
        return this._matching;
    }

    public String[] getTags() {
        return this._tags;
    }

    @Override
    public boolean isAllow(IService service) {
        String[] tags = new String[0];
        if (service instanceof ITagged) {
            tags = ((ITagged) service).getTags();
        }

        switch (this._model) {
            case INCLUDE:
                switch(this._matching) {
                    case SATISFY_ALL:
                        return CollectionHelper.isContainsAll(tags, this._tags);
                    case SATISFY_ANY:
                        return CollectionHelper.isContains(tags, this._tags);
                }
            case EXCLUDE:
                switch (this._matching) {
                    case SATISFY_ALL:
                        return ! CollectionHelper.isContainsAll(tags, this._tags);
                    case SATISFY_ANY:
                        return ! CollectionHelper.isContains(tags, this._tags);
                }
            default:
                throw new GeneralException("Unsupported model or matching - {}, {}", this._model, this._matching);
        }
    }

    public enum Model {
        /**
         * Include all satisfied services
         */
        INCLUDE,
        /**
         * Exclude all satisfied services
         */
        EXCLUDE;

        public static Model parse(String value) {
            if ("include".equalsIgnoreCase(value)) {
                return INCLUDE;
            } else if ("exclude".equalsIgnoreCase(value)) {
                return EXCLUDE;
            } else {
                throw AppException.builder()
                        .errorCode(AppErrors.UNSUPPORTED_PROFILE_MODEL)
                        .variables(new AppErrors.UnsupportedProfileModel()
                                .model(value))
                        .build();
            }
        }
    }

    public enum Matching {
        /**
         * All tags must be satisfied
         */
        SATISFY_ALL("satisfy-all"),
        /**
         * One of tags must be satisfied
         */
        SATISFY_ANY("satisfy-any");

        public static Matching parse(String value) {
            if (SATISFY_ALL._value.equalsIgnoreCase(value)) {
                return SATISFY_ALL;
            } else if (SATISFY_ANY._value.equalsIgnoreCase(value)) {
                return SATISFY_ANY;
            } else {
                throw AppException.builder()
                        .errorCode(AppErrors.UNSUPPORTED_PROFILE_MATCHING)
                        .variables(new AppErrors.UnsupportedProfileMatching()
                                .matching(value))
                        .build();
            }
        }

        private String _value;

        Matching(String value) {
            this._value = value;
        }
    }
}
