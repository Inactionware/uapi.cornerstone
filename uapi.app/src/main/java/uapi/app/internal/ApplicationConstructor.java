package uapi.app.internal;

import uapi.app.*;
import uapi.behavior.*;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.common.StringHelper;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.IService;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * The constructor is used to construct application's behavior
 */
@Service(autoActive = true)
@Tag("Application")
public class ApplicationConstructor {

    private static final String RESPONSIBLE_NAME            = "Application";

    private static final String BEHAVIOR_STARTUP            = "startUp";
    private static final String BEHAVIOR_SHUTDOWN           = "shutdown";

    private final IAnonymousAction<Exception, BehaviorEvent> DEFAULT_FAILURE_ACTION = (ex, ctx) -> {
        this._logger.error(ex, "Fail to process behavior - {}", ctx.behaviorName());
        return null;
    };

    @Inject
    protected ILogger _logger;

    @Inject
    protected IRegistry _registry;

    @Inject
    protected IResponsibleRegistry _responsibleReg;

    @OnActivate
    public void activate() {
        // Build responsible and related behavior for application launching
        IResponsible responsible = this._responsibleReg.register(RESPONSIBLE_NAME);
        responsible.newBehavior(BEHAVIOR_STARTUP, SystemStartingUpEvent.class, SystemStartingUpEvent.TOPIC)
                .then(StartupApplication.actionId)
                .onSuccess((input, execCtx) -> {
                    this._logger.info("Application startup success.");
                    return new AppStartupEvent(responsible.name());
                })
                .onFailure(DEFAULT_FAILURE_ACTION)
                .build();
        responsible.newBehavior(BEHAVIOR_SHUTDOWN, SystemShuttingDownEvent.class, SystemShuttingDownEvent.TOPIC)
                .then((input, ctx) -> {
                    this._logger.info("Application is going to shutdown...");
                    return input;
                })
                .onSuccess((input, ctx) ->
                        new AppShutdownEvent(responsible.name(), ((SystemShuttingDownEvent) input).applicationServices()))
                .onSuccessEventCallback(event -> {
                    List<IService> svcs = ((AppShutdownEvent) event).applicationServices();
                    List<String[]> svcIds = Looper.on(svcs).map(IService::getIds).toList();
                    Looper.on(svcIds).foreach(this._registry::deactivateServices);
                    this._logger.debug("Application shutdown success.");
                })
                .onFailure(DEFAULT_FAILURE_ACTION)
                .build();
    }

    /**
     * Action to start up application
     */
    @Service
    @Action
    @Tag("Application")
    public static class StartupApplication {

        public static final ActionIdentify actionId = ActionIdentify.toActionId(StartupApplication.class);

        @Inject
        protected ILogger _logger;

        @Inject
        protected IRegistry _registry;

        @ActionDo
        public void startup(SystemStartingUpEvent event) {
            this._logger.info("Application is going to startup...");
            ProfileManager profileMgr = this._registry.findService(ProfileManager.class);
            if (profileMgr == null) {
                throw AppException.builder()
                        .errorCode(AppErrors.SPECIFIC_SERVICE_NOT_FOUND)
                        .variables(new AppErrors.SpecificServiceNotFound()
                                .serviceType(ProfileManager.class.getCanonicalName()))
                        .build();
            }
            IProfile profile = profileMgr.getActiveProfile();

            List<String> autoActiveSvcIds = new ArrayList<>();

            // Register other service
            Looper.on(event.applicationServices())
                    .filter(profile::isAllow)
                    .next(svc -> {
                        if (svc.autoActive()) {
                            autoActiveSvcIds.add(svc.getIds()[0]);
                        }
                    })
                    .foreach(this._registry::register);

            // Activate auto active services
            Looper.on(autoActiveSvcIds).foreach(this._registry::findService);

            this._logger.info("The application is launched");
        }
    }
}
