package uapi.example.quickstart.app;

import uapi.app.IApplicationLifecycle;
import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;

/**
 * Hello world application demo
 */
@Service(autoActive=true)
public class HelloWorldConstructor {

    protected ILogger _logger;

    @Inject
    protected IResponsibleRegistry _responsibleReg;

    @OnActivate
    public void activate() {
        IResponsible responsible = this._responsibleReg.register("Hello Wold App");
    }
}
