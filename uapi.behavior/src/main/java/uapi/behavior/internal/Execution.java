package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.IIdentifiable;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.rx.Looper;

/**
 * The class represent a execution of one behavior
 */
public class Execution implements IIdentifiable<ExecutionIdentify> {

    private final ExecutionIdentify _id;
    private final boolean _traceable;
    private ActionHolder _current;

    private final IBehaviorSuccessCall _successAction;
    private final IBehaviorFailureCall _failureAction;

    Execution(
            final Behavior behavior,
            final int sequence,
            final IBehaviorSuccessCall successAction,
            final IBehaviorFailureCall failureAction
    ) {
        ArgumentChecker.required(behavior, "behavior");
        this._id = new ExecutionIdentify(behavior.getId(), sequence);
        this._traceable = behavior.traceable();
        this._current = behavior.entranceAction();
        this._successAction = successAction;
        this._failureAction = failureAction;
    }

    @Override
    public ExecutionIdentify getId() {
        return this._id;
    }

    /**
     * Execute current action until no subsequent action is available
     *
     * @param   behaviorInputs
     *          The input data
     * @param   executionContext
     *          The context of current execution
     */
    void execute(
            final Object[] behaviorInputs,
            final ActionOutput[] behaviorOutputs,
            final ExecutionContext executionContext
    ) {
        ArgumentChecker.required(executionContext, "executionContext");
        String sourceRespName = executionContext.get(IExecutionContext.KEY_RESP_NAME);
        Exception exception = null;
        Object[] actionInputs;
        ActionOutput[] outputs = null;
        try {
            do {
                if (this._current.previous() == null) {
                    // First action
                    actionInputs = behaviorInputs;
                } else {
                    actionInputs = this._current.inputs();
                }
                // create input objects
                ActionInputMeta[] inputMetas = this._current.inputMetas();
                for (int i = 0; i < actionInputs.length; i++) {
                    if (actionInputs[i] instanceof ActionInputReference) {
                        ActionInputReference actionInRef = (ActionInputReference) actionInputs[i];
                        String key = actionInRef.toKey();
                        actionInputs[i] = executionContext.get(key);
                    }
                }
                // create outputs
                ActionOutputMeta[] outMetas = this._current.action().outputMetas();
                if (outMetas.length == 0) {
                    outputs = new ActionOutput[0];
                } else {
                    outputs = new ActionOutput[outMetas.length];
                    for (int idx = 0; idx < outputs.length; idx++) {
                        outputs[idx] = new ActionOutput(this._current.action().getId(), outMetas[idx]);
                    }
                }
                // execute action
                this._current.action().process(actionInputs, outputs, executionContext);
                if (this._traceable) {
                    BehaviorExecutingEvent event = new BehaviorExecutingEvent(
                            sourceRespName, this._id, this._current.action().getId(), actionInputs, outputs, behaviorInputs);
                    executionContext.fireEvent(event);
                }

                // set output to execution context
                Looper.on(outputs).foreach(output -> {
                    String key = ActionInputReference.generateKey(this._current.label(), output.meta().name());
                    executionContext.put(key, output.get());
                });
                // find next action
                ActionOutputs outAttributes = new ActionOutputs(outputs);
                this._current = this._current.findNext(outAttributes);
            } while (this._current != null);
        } catch (Exception ex) {
            exception = ex;
            if (this._failureAction != null) {
                BehaviorEvent bEvent = null;
                try {
                    BehaviorFailure bFailure = new BehaviorFailure(this._current.action().getId(), behaviorInputs, ex);
                    bEvent = this._failureAction.accept(bFailure, executionContext);
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
                BehaviorSuccess bSuccess = new BehaviorSuccess(behaviorInputs, outputs);
                bEvent = this._successAction.accept(bSuccess, executionContext);
            } catch (Exception eex) {
                exception = new GeneralException(eex);
            }
            if (bEvent != null) {
                executionContext.fireEvent(bEvent);
            }
        }
        if (this._traceable) {
            BehaviorFinishedEvent event = new BehaviorFinishedEvent(
                    sourceRespName, this._id, behaviorInputs, outputs, exception);
            executionContext.fireEvent(event);
        }

        if (exception != null) {
            if (exception instanceof GeneralException) {
                throw (RuntimeException) exception;
            } else {
                throw new GeneralException(exception);
            }
        }
    }
}
