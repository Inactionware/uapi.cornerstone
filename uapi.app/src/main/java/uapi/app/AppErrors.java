/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app;

import uapi.exception.FileBasedExceptionErrors;
import uapi.exception.IndexedParameters;
import uapi.service.IRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Error codes for uapi.app module.
 */
public class AppErrors extends FileBasedExceptionErrors<AppException> {

    public static final int CATEGORY = 0x0101;

    public static final int REGISTRY_IS_REQUIRED            = 1;
    public static final int MORE_REGISTRY                   = 2;
    public static final int REGISTRY_IS_UNSATISFIED         = 3;
    public static final int INIT_APPLICATION_FAILED         = 4;
    public static final int UNSUPPORTED_PROFILE_MATCHING    = 5;
    public static final int UNSUPPORTED_PROFILE_MODEL       = 6;
    public static final int DUPLICATED_PROFILE              = 7;
    public static final int TAG_CONFIG_IS_NOT_LIST          = 8;
    public static final int SPECIFIC_SERVICE_NOT_FOUND      = 9;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(REGISTRY_IS_REQUIRED, RegistryIsRequired.KEY);
        keyCodeMapping.put(MORE_REGISTRY, MoreRegistry.KEY);
        keyCodeMapping.put(REGISTRY_IS_UNSATISFIED, RepositoryIsUnsatisfied.KEY);
        keyCodeMapping.put(INIT_APPLICATION_FAILED, InitApplicationFailed.KEY);
        keyCodeMapping.put(UNSUPPORTED_PROFILE_MATCHING, UnsupportedProfileMatching.KEY);
        keyCodeMapping.put(UNSUPPORTED_PROFILE_MODEL, UnsupportedProfileModel.KEY);
        keyCodeMapping.put(DUPLICATED_PROFILE, DuplicatedProfile.KEY);
        keyCodeMapping.put(TAG_CONFIG_IS_NOT_LIST, TagConfigIsNotList.KEY);
        keyCodeMapping.put(SPECIFIC_SERVICE_NOT_FOUND, SpecificServiceNotFound.KEY);
    }

    @Override
    protected String getFile(AppException exception) {
        if (exception.category() == CATEGORY) {
            return "/appErrors.properties";
        }
        return null;
    }

    @Override
    protected String getKey(AppException exception) {
        return keyCodeMapping.get(exception.errorCode());
    }

    /**
     * Error string template:
     *      A service instance of IRegistry must be provided
     */
    public static final class RegistryIsRequired extends IndexedParameters<RegistryIsRequired> {

        private static final String KEY = "RegistryIsRequired";
    }

    /**
     * Error string template:
     *      Found more than one IRegistry instance {}
     */
    public static final class MoreRegistry extends IndexedParameters<MoreRegistry> {

        private static final String KEY = "MoreRegistry";

        private List<IRegistry> _regs;

        public MoreRegistry registries(List<IRegistry> registries) {
            this._regs = registries;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._regs };
        }
    }

    /**
     * Error string template:
     *      The service repository can't be satisfied - {}
     */
    public static final class RepositoryIsUnsatisfied extends IndexedParameters<RepositoryIsUnsatisfied> {

        private static final String KEY = "RepositoryIsUnsatisfied";

        private String _svcRegType;

        public RepositoryIsUnsatisfied serviceRegistryType(String type) {
            this._svcRegType = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcRegType };
        }
    }

    /**
     * Error string template:
     *      The Application was not initialized
     */
    public static final class InitApplicationFailed extends IndexedParameters<InitApplicationFailed> {

        private static final String KEY = "InitApplicationFailed";
    }

    /**
     * Error string template:
     *      Unsupported profile tag matching policy - {}
     */
    public static final class UnsupportedProfileMatching extends IndexedParameters<UnsupportedProfileMatching> {

        private static final String KEY = "UnsupportedProfileMatching";

        private String _matching;

        public UnsupportedProfileMatching matching(String matching) {
            this._matching = matching;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._matching };
        }
    }

    /**
     * Error string template:
     *      Unsupported profile tag model policy - {}
     */
    public static final class UnsupportedProfileModel extends IndexedParameters<UnsupportedProfileModel> {

        private static final String KEY = "UnsupportedProfileModel";

        private String _model;

        public UnsupportedProfileModel model(String model) {
            this._model = model;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._model };
        }
    }

    /**
     * Error string template:
     *      Duplicated profile - {}
     */
    public static final class DuplicatedProfile extends IndexedParameters<DuplicatedProfile> {

        private static final String KEY = "DuplicatedProfile";

        private String _name;

        public DuplicatedProfile name(String name) {
            this._name = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._name };
        }
    }

    /**
     * Error string template:
     *      The tags configuration must be a List
     */
    public static final class TagConfigIsNotList extends IndexedParameters<TagConfigIsNotList> {

        private static final String KEY = "TagConfigIsNotList";
    }

    /**
     * Error string template:
     *      The specific service was not found in the registry - {}
     */
    public static final class SpecificServiceNotFound extends IndexedParameters<SpecificServiceNotFound> {

        private static final String KEY = "SpecificServiceNotFound";

        private String _svcType;

        public SpecificServiceNotFound serviceType(String type) {
            this._svcType = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._svcType };
        }
    }
}
