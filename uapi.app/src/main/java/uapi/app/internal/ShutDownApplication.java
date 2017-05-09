package uapi.app.internal;

import uapi.behavior.ActionIdentify;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Action to shut down application
 */
@Service
@Action
@Tag("Application")
public class ShutDownApplication {

    public static final ActionIdentify actionId = ActionIdentify.parse(ShutDownApplication.class.getName() + "@Action");

    @ActionDo
    public void shutdown(SystemShuttingDownEvent event) {
        // do nothing
    }
}
