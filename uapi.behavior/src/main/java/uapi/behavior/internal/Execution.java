package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.IIdentifiable;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.event.IEventFinishCallback;

/**
 * The class represent a execution of one behavior
 */
public class Execution implements IIdentifiable<ExecutionIdentify> {

    private final ExecutionIdentify _id;
    private final boolean _traceable;
    private ActionHolder _current;

    private final IAnonymousAction<Object, BehaviorEvent> _successAction;
    private final IAnonymousAction<Exception, BehaviorEvent> _failureAction;
    private final IEventFinishCallback _successEventCallback;

    Execution(
            final Behavior behavior,
            final int sequence,
            final IAnonymousAction<Object, BehaviorEvent> successAction,
            final IAnonymousAction<Exception, BehaviorEvent> failureAction,
            final IEventFinishCallback successEventCallback
    ) {
        ArgumentChecker.required(behavior, "behavior");
        this._id = new ExecutionIdentify(behavior.getId(), sequence);
        this._traceable = behavior.traceable();
        this._current = behavior.entranceAction();
        this._successAction = successAction;
        this._failureAction = failureAction;
        this._successEventCallback = successEventCallback;
    }

    @Override
    public ExecutionIdentify getId() {
        return this._id;
    }

    /**
     * Execute current action until no subsequent action is available
     *
     * @param   input
     *          The input data
     * @param   executionContext
     *          The context of current execution
     * @return  The output data
     */
    Object execute(
            final Object input,
            final ExecutionContext executionContext
    ) {
        ArgumentChecker.required(executionContext, "executionContext");
        Object output = input;
        String sourceRespName = executionContext.get(IExecutionContext.KEY_RESP_NAME);
        Exception exception = null;
        try {
            do {
                output = this._current.action().process(output, executionContext);
                if (this._traceable) {
                    BehaviorExecutingEvent event = new BehaviorExecutingEvent(
                            this._id, input, output, this._current.action().getId(), sourceRespName);
                    executionContext.fireEvent(event);
                }
                this._current = this._current.findNext(output);
            } while (this._current != null);
        } catch (Exception ex) {
            exception = ex;
            if (this._failureAction != null) {
                BehaviorEvent bEvent = null;
                try {
                    bEvent = this._failureAction.accept(ex, executionContext);
                } catch (Exception eex) {
                    exception = new GeneralException(eex);
                }
                if (bEvent != null) {
                    executionContext.fireEvent(bEvent);
                }
            }
        }
        if (this._successAction != null) {
            BehaviorEvent bEvent = null;
            try {
                bEvent = this._successAction.accept(output, executionContext);
            } catch (Exception eex) {
                exception = new GeneralException(eex);
            }
            if (bEvent != null) {
                if (this._successEventCallback != null) {
                    executionContext.fireEvent(bEvent, this._successEventCallback);
                } else {
                    executionContext.fireEvent(bEvent);
                }
            }
        }
        if (this._traceable) {
            BehaviorFinishedEvent event = new BehaviorFinishedEvent(
                    this._id, input, output, sourceRespName, exception);
            executionContext.fireEvent(event);
        }
        if (exception != null) {
            if (exception instanceof GeneralException) {
                throw (RuntimeException) exception;
            } else {
                throw new GeneralException(exception);
            }
        }
        return output;
    }
}
