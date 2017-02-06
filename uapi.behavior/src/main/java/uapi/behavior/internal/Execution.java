package uapi.behavior.internal;

import uapi.IIdentifiable;
import uapi.behavior.ActionIdentify;
import uapi.behavior.BehaviorExecutingEvent;
import uapi.behavior.BehaviorFinishedEvent;
import uapi.common.ArgumentChecker;

/**
 * The class represent a execution of one behavior
 */
public class Execution implements IIdentifiable<ExecutionIdentify> {

    private final ExecutionIdentify _id;
    private final boolean _traceable;
    private ActionHolder _current;

    Execution(final Behavior behavior, final int sequence) {
        ArgumentChecker.required(behavior, "behavior");
        this._id = new ExecutionIdentify(behavior.getId(), sequence);
        this._traceable = behavior.traceable();
        this._current = behavior.entryAction();
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
        Object output;
        do {
            output = this._current.action().process(input, executionContext);
            if (this._traceable) {
                BehaviorExecutingEvent event = new BehaviorExecutingEvent(
                        this._id, input, output, (ActionIdentify) this._current.action().getId());
                executionContext.fireEvent(event);
            }
            this._current = this._current.findNext(output);
        } while (this._current != null);
        if (this._traceable) {
            BehaviorFinishedEvent event = new BehaviorFinishedEvent(this._id, input, output);
            executionContext.fireEvent(event);
        }
        return output;
    }
}
