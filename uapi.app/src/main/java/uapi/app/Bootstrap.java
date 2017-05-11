/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app;

import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.app.internal.*;
import uapi.behavior.ActionIdentify;
import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.config.ICliConfigProvider;
import uapi.common.CollectionHelper;
import uapi.event.IEventBus;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.IService;
import uapi.service.ITagged;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The UAPI application entry point
 * The Bootstrap's responsibility is load basic services and all other services is loaded by
 * profile definition
 */
public class Bootstrap {

    private static final String[] basicSvcTags = new String[] {
            "Registry", "Config", "Log", "Event", "Behavior", "Application", "Profile"
    };

    static final AppServiceLoader appSvcLoader = new AppServiceLoader();
    static final Semaphore semaphore = new Semaphore(0);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        Iterable<IService> svcLoaders = appSvcLoader.loadServices();
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
        if (cliCfgProvider == null) {
            throw AppException.builder()
                    .errorCode(AppErrors.SPECIFIC_SERVICE_NOT_FOUND)
                    .variables(new AppErrors.SpecificServiceNotFound()
                            .serviceType(ICliConfigProvider.class.getCanonicalName()))
                    .build();
        }
        cliCfgProvider.parse(args);

        // All base service must be activated
        Looper.on(basicSvcTags).foreach(svcRegistry::activateTaggedService);

        // Build responsible and related behavior for application launching
//        IResponsibleRegistry responsibleReg = svcRegistry.findService(IResponsibleRegistry.class);
//        IResponsible responsible = responsibleReg.register("ApplicationHandler");
//        responsible.newBehavior("startUpApplication", SystemStartingUpEvent.TOPIC)
//                .then(ActionIdentify.parse(StartupApplication.class.getName() + "@Action"))
//                .build();
//        responsible.newBehavior("shutDownApplication", SystemShuttingDownEvent.TOPIC)
//                .then(ActionIdentify.parse(ShutdownApplication.class.getName() + "@Action"))
//                .build();

        // Send system starting up event
        SystemStartingUpEvent sysLaunchedEvent = new SystemStartingUpEvent(startTime, otherSvcs);
        IEventBus eventBus = svcRegistry.findService(IEventBus.class);
        eventBus.fire(sysLaunchedEvent);

        Exception ex = null;
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
            semaphore.acquire();
        } catch (InterruptedException e) {
            ex = e;
        }

        // Send system shutting down event
        SystemShuttingDownEvent shuttingDownEvent = new SystemShuttingDownEvent(ex);
        eventBus.fire(shuttingDownEvent);

        System.exit(0);

//        // Create profile
//        ProfileManager profileMgr = svcRegistry.findService(ProfileManager.class);
//        if (profileMgr == null) {
//            throw AppException.builder()
//                    .errorCode(AppErrors.SPECIFIC_SERVICE_NOT_FOUND)
//                    .variables(new AppErrors.SpecificServiceNotFound()
//                            .serviceType(ICliConfigProvider.class.getCanonicalName()))
//                    .build();
//        }
//        IProfile profile = profileMgr.getActiveProfile();
//
//        // Register other service
//        Looper.on(otherSvcs)
//                .filter(profile::isAllow)
//                .foreach(svcRegistry::register);
//
//        Application app = svcRegistry.findService(Application.class);
//        if (app == null) {
//            throw AppException.builder()
//                    .errorCode(AppErrors.INIT_APPLICATION_FAILED)
//                    .build();
//        }
//        app.startup(startTime);
    }

    private static final class ShutdownHook implements Runnable {

        @Override
        public void run() {
            semaphore.release();
        }
    }
}
