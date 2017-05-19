package uapi.app.internal;

import uapi.app.EventHandlingFinishedEvent;
import uapi.app.PublishEventEvent;
import uapi.behavior.ActionIdentify;
import uapi.behavior.IExecutionContext;
import uapi.behavior.IResponsible;
import uapi.behavior.IResponsibleRegistry;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.event.IEventBus;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;

/**
 * Created by xquan on 5/19/2017.
 */
@Service(autoActive = true)
public class EventPublisher {

    @Inject
    protected IEventBus _eventBus;

    @Inject
    protected IResponsibleRegistry _respReg;

    @OnActivate
    public void activate() {
        IResponsible resp = this._respReg.register("Event Publisher");
        resp.newBehavior("Publish Event", PublishEventEvent.class, PublishEventEvent.TOPIC)
                .then(PublishEventAction.actionId)
                .traceable(true)
                .build();
    }

    @Service
    @Action
    public static class PublishEventAction {

        private static final ActionIdentify actionId = ActionIdentify.toActionId(PublishEventAction.class);

        @Inject
        protected IEventBus _eventBus;

        @ActionDo
        public void publish(
                final PublishEventEvent event,
                final IExecutionContext execCtx
        ) {
            if (event.needNotify()) {
                this._eventBus.fire(event, () -> {
                    EventHandlingFinishedEvent finEvent = new EventHandlingFinishedEvent(execCtx.responsibleName());
                    this._eventBus.fire(finEvent);
                });
            } else {
                this._eventBus.fire(event);
            }
        }
    }
}
