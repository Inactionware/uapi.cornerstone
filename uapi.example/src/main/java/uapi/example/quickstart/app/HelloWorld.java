package uapi.example.quickstart.app;

import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Hello world application demo
 */
@Service(autoActive=true)
@Tag("Hello")
public class HelloWorld {

    @Inject
    protected ILogger _logger;

    @OnActivate
    public void activate() {
        this._logger.info("Hello World");
    }
}
