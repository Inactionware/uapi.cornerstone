package uapi.example.quickstart.app;

import uapi.log.ILogger;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;

/**
 * Hello world application demo
 */
@Service(autoActive=true)
public class HelloWorld {

    protected ILogger _logger;

    @OnActivate
    public void activate() {
        this._logger.info("Hello World!");
    }
}
