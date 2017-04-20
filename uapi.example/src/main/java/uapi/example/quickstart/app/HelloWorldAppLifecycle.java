package uapi.example.quickstart.app;

import uapi.app.IApplicationLifecycle;
import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;

/**
 * Hello world application demo
 */
@Service(IApplicationLifecycle.class)
public class HelloWorldAppLifecycle implements IApplicationLifecycle {

    @Inject
    protected ILogger _logger;

    @Override
    public String getApplicationName() {
        return "Hello World Application";
    }

    @Override
    public void onStarted() {
        this._logger.info("{} is started.", getApplicationName());
    }

    @Override
    public void onStopped() {
        this._logger.info("{} is stopped.", getApplicationName());
    }
}
