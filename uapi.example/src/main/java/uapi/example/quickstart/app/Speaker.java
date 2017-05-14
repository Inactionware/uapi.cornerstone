package uapi.example.quickstart.app;

import uapi.app.AppStartupEvent;
import uapi.behavior.ActionIdentify;
import uapi.behavior.ActionType;
import uapi.behavior.IExecutionContext;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.common.StringHelper;
import uapi.log.ILogger;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Created by xquan on 5/11/2017.
 */
@Service
@Action
@Tag("BabyHello")
public class Speaker {

    public static final ActionIdentify actionId = ActionIdentify.parse(
            StringHelper.makeString("{}@{}", Speaker.class.getName(), ActionType.ACTION));

    @Inject
    protected ILogger _logger;

    @ActionDo
    public void say(AppStartupEvent event, IExecutionContext execCtx) {
        this._logger.info("Hello World form {}", execCtx.responsibleName());
    }
}
