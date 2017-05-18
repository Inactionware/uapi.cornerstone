package uapi.app.internal;

import uapi.app.AppErrors;
import uapi.app.AppException;
import uapi.app.AppStartupEvent;
import uapi.behavior.BehaviorEvent;
import uapi.behavior.BehaviorFinishedEventHandler;
import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * The constructor is used to construct application's behavior
 */
@Service(autoActive = true)
@Tag("Application")
public class ApplicationConstructor {

    private static final String RESPONSIBLE_NAME    = "Application";

    private static final String BEHAVIOR_STARTUP    = "startUp";
    private static final String BEHAVIOR_SHUTDOWN   = "shutdown";

    @Inject
    protected ILogger _logger;

    @Inject
    protected IResponsibleRegistry _responsibleReg;

    @OnActivate
    public void activate() {
        // Build responsible and related behavior for application launching
        IResponsible responsible = this._responsibleReg.register(RESPONSIBLE_NAME);
        responsible.newBehavior(BEHAVIOR_STARTUP, SystemStartingUpEvent.class, SystemStartingUpEvent.TOPIC)
                .then(StartupApplication.actionId)
                .traceable(true)
                .build();
        responsible.newBehavior(BEHAVIOR_SHUTDOWN, SystemShuttingDownEvent.class, SystemShuttingDownEvent.TOPIC)
                .then(ShutdownApplication.actionId)
                .traceable(true)
                .build();

        BehaviorFinishedEventHandler finishedHandler = event -> {
            BehaviorEvent bEvent = null;
            if (BEHAVIOR_STARTUP.equals(event.behaviorName())) {
                bEvent = new AppStartupEvent(responsible.name());
                this._logger.info("Startup Application success.");
            } else if (BEHAVIOR_SHUTDOWN.equals(event.behaviorName())) {
                this._logger.debug("Application shutdown success.");
            } else {
                // It should not happen
                throw AppException.builder()
                        .errorCode(AppErrors.UNSUPPORTED_RESPONSIBLE_BEHAVIOR)
                        .variables(new AppErrors.UnsupportedResponsibleBehavior()
                                .behaviorName(event.behaviorName())
                                .responsibleName(responsible.name()))
                        .build();
            }
            return bEvent;
        };
        responsible.on(finishedHandler);
    }
}
