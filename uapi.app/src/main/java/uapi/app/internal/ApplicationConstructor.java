package uapi.app.internal;

import uapi.GeneralException;
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

    private static final String BEHAVIOR_STARTUP    = "startUpApplication";
    private static final String BEHAVIOR_SHUTDOWN   = "shutDownApplication";

    private static final String EVENT_APP_STARTUP   = "ApplicationStartup";

    @Inject
    protected ILogger _logger;

    @Inject
    protected IResponsibleRegistry _responsibleReg;

    @OnActivate
    public void activate() {
        // Build responsible and related behavior for application launching
        IResponsible responsible = this._responsibleReg.register("ApplicationHandler");
        responsible.newBehavior(BEHAVIOR_STARTUP, SystemStartingUpEvent.TOPIC)
                .then(StartupApplication.actionId)
                .build();
        responsible.newBehavior(BEHAVIOR_SHUTDOWN, SystemShuttingDownEvent.TOPIC)
                .then(ShutDownApplication.actionId)
                .build();

        BehaviorFinishedEventHandler finishedHandler = event -> {
            BehaviorEvent bEvent = null;
            if (BEHAVIOR_STARTUP.equals(event.behaviorName())) {
                bEvent = new AppStartupEvent(responsible.name());
            } else if (BEHAVIOR_SHUTDOWN.equals(event.behaviorName())) {
                this._logger.debug("Application is going to shutdown");
            } else {
                // It should not happen
                throw new GeneralException(
                        "Unsupported behavior trace event - {}", event.behaviorName());
            }
            return bEvent;
        };
        responsible.on(finishedHandler);
    }
}
