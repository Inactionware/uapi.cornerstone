package uapi.app.internal;

import uapi.app.*;
import uapi.behavior.*;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
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

    private static final String RESPONSIBLE_NAME            = "Application";

    private static final String BEHAVIOR_STARTUP            = "startUp";
    private static final String BEHAVIOR_SHUTDOWN           = "shutdown";
    private static final String BEHAVIOR_NOTIFY_SHUTDOWN    = "notifyShutdown";

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
        responsible.newBehavior(BEHAVIOR_NOTIFY_SHUTDOWN, SystemShuttingDownEvent.class, SystemShuttingDownEvent.TOPIC)
//                .then(NotifyShutdownAction.actionId)
                .traceable(true)
                .build();
        responsible.newBehavior(BEHAVIOR_SHUTDOWN, EventHandlingFinishedEvent.class, EventHandlingFinishedEvent.TOPIC)
                .then(ShutdownApplication.actionId)
                .traceable(true)
                .build();

        BehaviorFinishedEventHandler finishedHandler = event -> {
            BehaviorEvent bEvent = null;
            if (BEHAVIOR_STARTUP.equals(event.behaviorName())) {
                bEvent = new AppStartupEvent(responsible.name());
                this._logger.info("Startup Application success.");
            } else if (BEHAVIOR_NOTIFY_SHUTDOWN.equals(event.behaviorName())) {
                bEvent = new PublishEventEvent(responsible.name(), new AppShutdownEvent(responsible.name()), true);
                this._logger.info("Publish shutdown event to system");
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

//    @Service
//    @Action
//    @Tag("Application")
//    public static class NotifyShutdownAction {
//
//        public static final ActionIdentify actionId = ActionIdentify.toActionId(NotifyShutdownAction.class);
//
//        @ActionDo
//        public void notify(SystemShuttingDownEvent event, IExecutionContext execCtx) {
//            // do nothing
//        }
//    }
}
