package uapi.app.internal;

import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

/**
 * Created by xquan on 4/28/2017.
 */

@Service
@Action
@Tag("Application")
public class ShutDownApplication {

    @ActionDo
    public void exec(SystemShuttingDownEvent event) {

    }
}
