package uapi.app;

import uapi.Tags;
import uapi.app.internal.AppServiceLoader;
import uapi.app.internal.SystemStartingUpEvent;
import uapi.common.CollectionHelper;
import uapi.event.IEventBus;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.IService;
import uapi.service.ITagged;

import java.util.ArrayList;
import java.util.List;

/**
 * Use to load service and activate system services
 */
public abstract class SystemBootstrap {

    private static final String[] basicSvcTags = new String[] {
            Tags.REGISTRY,
            Tags.CONFIG,
            Tags.LOG,
            Tags.EVENT,
            Tags.BEHAVIOR,
            Tags.PROFILE,
            Tags.APPLICATION
    };

    private static AppServiceLoader appSvcLoader = new AppServiceLoader();

    /**
     * Load all service and activate system services and return application service list
     *
     * @return  Application service list
     * @throws  AppException
     *          When service registry can't be found or found more service registry instance
     */
    public void boot() throws AppException {
        long startTime = System.currentTimeMillis();

        Iterable<IService> svcLoaders = appSvcLoader.loadServices();
        final List<IRegistry> svcRegistries = new ArrayList<>();
        final List<IService> sysSvcs = new ArrayList<>();
        final List<IService> appSvcs = new ArrayList<>();
        Looper.on(svcLoaders)
                .foreach(svc -> {
                    if (svc instanceof IRegistry) {
                        svcRegistries.add((IRegistry) svc);
                    }
                    if (svc instanceof ITagged) {
                        ITagged taggedSvc = (ITagged) svc;
                        String[] tags = taggedSvc.getTags();
                        if (CollectionHelper.contains(tags, basicSvcTags) != null) {
                            sysSvcs.add(svc);
                        } else {
                            appSvcs.add(svc);
                        }
                    } else {
                        appSvcs.add(svc);
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
        svcRegistry.register(sysSvcs.toArray(new IService[sysSvcs.size()]));
        String svcRegType = svcRegistry.getClass().getCanonicalName();
        svcRegistry = svcRegistry.findService(IRegistry.class);
        if (svcRegistry == null) {
            throw AppException.builder()
                    .errorCode(AppErrors.REGISTRY_IS_UNSATISFIED)
                    .variables(new AppErrors.RepositoryIsUnsatisfied()
                            .serviceRegistryType(svcRegType))
                    .build();
        }

        loadConfig();

        // All base service must be activated
        Looper.on(basicSvcTags).foreach(svcRegistry::activateTaggedService);

        beforeSystemLaunching();

        // Send system starting up event
        SystemStartingUpEvent sysLaunchedEvent = new SystemStartingUpEvent(startTime, appSvcs);
        IEventBus eventBus = svcRegistry.findService(IEventBus.class);
//        eventBus.register(new ExitSystemRequestHandler());
        eventBus.fire(sysLaunchedEvent);

        afterSystemLaunching();
    }

    protected abstract void loadConfig();

    protected abstract void beforeSystemLaunching();

    protected abstract void afterSystemLaunching();
}
