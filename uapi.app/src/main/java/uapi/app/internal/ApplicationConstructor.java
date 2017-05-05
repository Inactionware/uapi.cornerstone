package uapi.app.internal;

import uapi.behavior.ActionIdentify;
import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Created by xquan on 5/5/2017.
 */
@Service
@Tag("Application")
public class ApplicationConstructor {

    @Inject
    protected IResponsibleRegistry _responsibleReg;

    @OnActivate
    public void activate() {
        // Build responsible and related behavior for application launching
        IResponsible responsible = this._responsibleReg.register("ApplicationHandler");
        responsible.newBehavior("startUpApplication", SystemStartingUpEvent.TOPIC)
                .then(StartupApplication.actionId)
                .build();
        responsible.newBehavior("shutDownApplication", SystemShuttingDownEvent.TOPIC)
                .then(ActionIdentify.parse(ShutDownApplication.class.getName() + "@Action"))
                .build();
    }
}
