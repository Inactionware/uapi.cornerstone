package uapi.app.internal;

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
            if (BEHAVIOR_STARTUP.equals(event.behaviorName())) {
                // Todo: fire application startup event
                return new BehaviorEvent("ApplicationStartup");
            } else {
                this._logger.debug("");
            }
            return null;
        };
        responsible.on(finishedHandler);
    }
}
