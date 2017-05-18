package uapi.app.internal;

import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.behavior.ActionIdentify;
import uapi.behavior.ActionType;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.common.StringHelper;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to start up application
 */
@Service
@Action
@Tag("Application")
public class StartupApplication {

    public static final ActionIdentify actionId = ActionIdentify.parse(
            StringHelper.makeString("{}@{}", StartupApplication.class.getName(), ActionType.ACTION));

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
