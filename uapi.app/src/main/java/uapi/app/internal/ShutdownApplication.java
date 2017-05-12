package uapi.app.internal;

import uapi.behavior.ActionIdentify;
import uapi.behavior.ActionType;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.IService;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.List;

/**
 * Action to shut down application
 */
@Service
@Action
@Tag("Application")
public class ShutdownApplication {

    public static final ActionIdentify actionId = ActionIdentify.parse(
            StringHelper.makeString("{}@{}", ShutdownApplication.class.getName(), ActionType.ACTION));

    @Inject
    protected IRegistry _registry;

    @ActionDo
    public void shutdown(SystemShuttingDownEvent event) {
        List<IService> svcs = event.applicationServices();
        List<String[]> svcIds = Looper.on(svcs).map(IService::getIds).toList();
        Looper.on(svcIds).foreach(this._registry::deactivateServices);
    }
}
