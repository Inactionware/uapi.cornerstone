package uapi.behavior.internal;

import uapi.common.ArgumentChecker;

/**
 * The class represent a execution of one behavior
 */
public class Execution {

    private ActionHolder _current;

    Execution(ActionHolder entryAction) {
        ArgumentChecker.required(entryAction, "entryAction");
        this._current = entryAction;
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
            this._current = this._current.findNext(output);
        } while (this._current != null);
        return output;
    }
}
