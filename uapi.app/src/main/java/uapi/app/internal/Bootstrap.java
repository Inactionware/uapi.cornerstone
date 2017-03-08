/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal;

import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.config.ICliConfigProvider;
import uapi.common.CollectionHelper;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.IService;
import uapi.service.ITagged;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The UAPI application entry point
 * The Bootstrap's responsibility is load basic services and all other services is loaded by
 * profile definition
 */
public class Bootstrap {

    private static final String[] basicSvcTags = new String[] {
            "Application", "Registry", "Config", "Profile", "Log"
    };

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        ServiceLoader<IService> svcLoaders = ServiceLoader.load(IService.class);
        final List<IRegistry> svcRegistries = new ArrayList<>();
        final List<IService> basicSvcs = new ArrayList<>();
        final List<IService> otherSvcs = new ArrayList<>();
        Looper.on(svcLoaders)
                .foreach(svc -> {
                    if (svc instanceof IRegistry) {
                        svcRegistries.add((IRegistry) svc);
                    }
                    if (svc instanceof ITagged) {
                        ITagged taggedSvc = (ITagged) svc;
                        String[] tags = taggedSvc.getTags();
                        if (CollectionHelper.contains(tags, basicSvcTags) != null) {
                            basicSvcs.add(svc);
                        } else {
                            otherSvcs.add(svc);
                        }
                    } else {
                        otherSvcs.add(svc);
                    }
                });

        if (svcRegistries.size() == 0) {
            throw AppException.builder()
                    .errorCode(AppErrors.REGISTRY_IS_REQUIRED)
                    .build();
        }
        if (svcRegistries.size() > 1) {
            throw AppException.builder()
                    .errorCode(AppErrors.MORE_REGISTRY)
                    .variables(new AppErrors.MoreRegistry()
                            .registries(svcRegistries))
                    .build();
        }

        IRegistry svcRegistry = svcRegistries.get(0);
        // Register basic service first
        svcRegistry.register(basicSvcs.toArray(new IService[basicSvcs.size()]));
        String svcRegType = svcRegistry.getClass().getCanonicalName();
        svcRegistry = svcRegistry.findService(IRegistry.class);
        if (svcRegistry == null) {
            throw AppException.builder()
                    .errorCode(AppErrors.REGISTRY_IS_UNSATISFIED)
                    .variables(new AppErrors.RepositoryIsUnsatisfied()
                            .serviceRegistryType(svcRegType))
                    .build();
        }

        // Parse command line parameters
        ICliConfigProvider cliCfgProvider = svcRegistry.findService(ICliConfigProvider.class);
        cliCfgProvider.parse(args);

        // Create profile
        ProfileManager profileMgr = svcRegistry.findService(ProfileManager.class);
        IProfile profile = profileMgr.getActiveProfile();

        // Register other service
        Looper.on(otherSvcs)
                .filter(profile::isAllow)
                .foreach(svcRegistry::register);

        svcRegistry.start();

        Application app = svcRegistry.findService(Application.class);
        if (app == null) {
            throw AppException.builder()
                    .errorCode(AppErrors.INIT_APPLICATION_FAILED)
                    .build();
        }
        app.startup(startTime);
    }
}
