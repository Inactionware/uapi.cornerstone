package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.IIdentifiable;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;

/**
 * The class represent a execution of one behavior
 */
public class Execution implements IIdentifiable<ExecutionIdentify> {

    private final ExecutionIdentify _id;
    private final boolean _traceable;
    private ActionHolder _current;

    private final IAction _successAction;
    private final IAction _failureAction;

    Execution(
            final Behavior behavior,
            final int sequence,
            final IAction successAction,
            final IAction failureAction
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
     * @param   input
     *          The input data
     * @param   executionContext
     *          The context of current execution
     * @return  The output data
     */
    ActionResult execute(
            final Object[] inputs,
            final ActionOutput[] behaviorOutputs,
            final ExecutionContext executionContext
    ) {
        ArgumentChecker.required(executionContext, "executionContext");
        ActionResult result = null;
        String sourceRespName = executionContext.get(IExecutionContext.KEY_RESP_NAME);
        Exception exception = null;
        Object[] inParams;
        try {
            do {
                if (this._current.previous() == null) {
                    // First action
                    inParams = inputs;
                } else {
                    inParams = this._current.inputs();
                }
                // create input objects
                ActionInputMeta[] inputMetas = this._current.inputMetas();
                for (int i = 0; i < inParams.length; i++) {
                    if (inParams[i] instanceof ActionInputReference) {
                        ActionInputReference actionInRef = (ActionInputReference) inParams[i];
                        String key = actionInRef.toKey();
                        inParams[i] = executionContext.get(key);
                    }
                }
                // create outputs
                ActionOutputMeta[] outMetas = this._current.action().outputMetas();
                ActionOutput[] outputs;
                if (outMetas.length == 0) {
                    outputs = new ActionOutput[0];
                } else {
                    outputs = new ActionOutput[outMetas.length];
                    Looper.on(outMetas).foreachWithIndex((idx, outMeta) -> outputs[idx] = new ActionOutput(outMeta));
                }
                // execute action
                result = this._current.action().process(inputs, outputs, executionContext);
                if (this._traceable) {
                    BehaviorExecutingEvent event = new BehaviorExecutingEvent(
                            this._id, inputs, result, this._current.action().getId(), sourceRespName);
                    executionContext.fireEvent(event);
                }
                // set output to execution context
                Looper.on(outputs).foreach(output -> {
                    String key = ActionInputReference.generateKey(this._current.label(), output.name());
                    executionContext.put(key, output.get());
                });
                this._current = this._current.findNext(result);
            } while (this._current != null);
        } catch (Exception ex) {
            exception = ex;
            if (this._failureAction != null) {
                BehaviorEvent bEvent = null;
                try {
                    BehaviorFailure bFailure = new BehaviorFailure(this._current.action().getId(), inputs, ex);
                    bEvent = this._failureAction.process(bFailure, executionContext);
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
                BehaviorSuccess bSuccess = new BehaviorSuccess(output);
                bEvent = this._successAction.process(bSuccess, executionContext);
            } catch (Exception eex) {
                exception = new GeneralException(eex);
            }
            if (bEvent != null) {
                executionContext.fireEvent(bEvent);
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
        return result;
    }
}
