package uapi.app.internal;

import uapi.behavior.ActionIdentify;
import uapi.behavior.ActionType;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.common.StringHelper;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Action to shut down application
 */
@Service
@Action
@Tag("Application")
public class ShutdownApplication {

    public static final ActionIdentify actionId = ActionIdentify.parse(
            StringHelper.makeString("{}@{}", ShutdownApplication.class.getName(), ActionType.ACTION));

    @ActionDo
    public void shutdown(SystemShuttingDownEvent event) {
        // do nothing
    }
}
