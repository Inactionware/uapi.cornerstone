package uapi.app;

import uapi.IModulePortal;
import uapi.service.*;
import uapi.app.internal.AppServiceLoader;
import uapi.app.internal.SystemStartingUpEvent;
import uapi.common.CollectionHelper;
import uapi.event.IEventBus;
import uapi.rx.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Use to load service and activate system services
 */
public abstract class SystemBootstrap {

    private static final String[] sysSvcTags = new String[] {
            Tags.REGISTRY,
            Tags.CONFIG,
            Tags.LOG,
            Tags.EVENT,
            Tags.BEHAVIOR,
            Tags.PROFILE,
            Tags.APPLICATION
    };

    private static AppServiceLoader appSvcLoader = new AppServiceLoader();

    private IRegistry _registry;

    protected IRegistry registry() {
        return this._registry;
    }

    /**
     * Load all service and activate system services and return application service list
     *
     * @throws  AppException
     *          When service registry can't be found or found more service registry instance
     */
    public void boot() throws AppException {
        long startTime = System.currentTimeMillis();

        Iterable<IModulePortal> modulePortals = appSvcLoader.load(IModulePortal.class);
        List<IService> services = new ArrayList<>();
        Looper.on(modulePortals).foreach(modulePortal -> {
            if (modulePortal instanceof IServiceModulePortal) {
                Looper.on(((IServiceModulePortal) modulePortal).loadService()).foreach(services::add);
            } else {
                throw AppException.builder()
                        .errorCode(AppErrors.UNSUPPORTED_MODULE_PORTAL)
                        .variables(new AppErrors.UnsupportedModulePortal().portal(modulePortal))
                        .build();
            }
        });

//        Iterable<IService> svcLoaders = appSvcLoader.loadServices();
        final var svcRegistries = new ArrayList<IRegistry>();
        final var sysSvcs = new ArrayList<IService>();
        final var appSvcs = new ArrayList<IService>();
        Looper.on(services)
                .foreach(svc -> {
                    if (svc instanceof IRegistry) {
                        svcRegistries.add((IRegistry) svc);
                    }
                    if (svc instanceof ITagged) {
                        var taggedSvc = (ITagged) svc;
                        var tags = taggedSvc.getTags();
                        if (CollectionHelper.contains(tags, sysSvcTags) != null) {
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

        var svcRegistry = svcRegistries.get(0);
        // Register basic service first
        svcRegistry.register(sysSvcs.toArray(new IService[0]));
        var svcRegType = svcRegistry.getClass().getCanonicalName();
        svcRegistry = svcRegistry.findService(IRegistry.class);
        if (svcRegistry == null) {
            throw AppException.builder()
                    .errorCode(AppErrors.REGISTRY_IS_UNSATISFIED)
                    .variables(new AppErrors.RepositoryIsUnsatisfied()
                            .serviceRegistryType(svcRegType))
                    .build();
        }
        this._registry = svcRegistry;

        loadConfig(svcRegistry);

        // All base service must be activated
        Looper.on(sysSvcTags).foreach(svcRegistry::activateTaggedService);

        beforeSystemLaunching(svcRegistry, appSvcs);

        // Send system starting up event
        SystemStartingUpEvent sysLaunchedEvent = new SystemStartingUpEvent(startTime, appSvcs);
        var eventBus = svcRegistry.findService(IEventBus.class);
        eventBus.fire(sysLaunchedEvent);

        afterSystemLaunching(svcRegistry, appSvcs);
    }

    protected abstract void loadConfig(IRegistry registry);

    protected abstract void beforeSystemLaunching(IRegistry registry, List<IService> appServices);

    protected abstract void afterSystemLaunching(IRegistry registry, List<IService> appServices);
}
