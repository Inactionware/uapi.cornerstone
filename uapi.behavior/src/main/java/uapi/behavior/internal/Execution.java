package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.IIdentifiable;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.common.Attributed;
import uapi.rx.Looper;

/**
 * The class represent a execution of one behavior
 */
public class Execution implements IIdentifiable<ExecutionIdentify> {

    private final ExecutionIdentify _id;
    private final boolean _traceable;
    private ActionHolder _current;

    private final Functionals.BehaviorSuccessAction _successAction;
    private final Functionals.BehaviorFailureAction _failureAction;

    Execution(
            final Behavior behavior,
            final int sequence,
            final Functionals.BehaviorSuccessAction successAction,
            final Functionals.BehaviorFailureAction failureAction
    ) {
        ArgumentChecker.required(behavior, "behavior");
        this._id = new ExecutionIdentify(behavior.getId(), sequence);
        this._traceable = behavior.traceable();
        this._current = behavior.headAction();
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
        executionContext.put(IExecutionContext.KEY_BEHA_INPUTS, behaviorInputs);
        String sourceRespName = executionContext.get(IExecutionContext.KEY_RESP_NAME);
        Exception exception = null;
        Object[] actionInputs;
        ActionOutput[] actionOutputs = null;
        try {
            do {
                actionInputs = this._current.inputs();
                // create input objects
                for (int i = 0; i < actionInputs.length; i++) {
                    Object input = actionInputs[i];
                    if (input instanceof IOutputReference) {
                        input = executionContext.getOutput((IOutputReference) actionInputs[i]);
                        actionInputs[i] = input;
                    }
                }
                // create outputs
                ActionOutputMeta[] outMetas = this._current.action().outputMetas();
                if (outMetas.length == 0) {
                    actionOutputs = new ActionOutput[0];
                } else {
                    actionOutputs = new ActionOutput[outMetas.length];
                    for (int idx = 0; idx < actionOutputs.length; idx++) {
                        actionOutputs[idx] = new ActionOutput(this._current.action().getId(), outMetas[idx]);
                    }
                }
                // execute action
                this._current.action().process(actionInputs, actionOutputs, executionContext);
                if (this._traceable) {
                    BehaviorExecutingEvent event = new BehaviorExecutingEvent(
                            sourceRespName, this._id, this._current.action().getId(), actionInputs, actionOutputs, behaviorInputs);
                    executionContext.fireEvent(event);
                }

                // set output to execution context
                Object[] outputs = Looper.on(actionOutputs).map(ActionOutput::get).toArray();
                ActionOutputHolder outHolder = new ActionOutputHolder(outMetas, outputs);
                executionContext.setOutputs(this._current.label(), outHolder);

                // find next action
                final ActionOutput[] tmp = actionOutputs;
                Attributed outAttrs = Attributed.apply(
                        attr -> Looper.on(tmp).foreachWithIndex((idx, output) ->
                            attr.set(output.meta().name() != null ? output.meta().name() : idx, output.get())
                        ));
                this._current = this._current.findNext(outAttrs);
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
                BehaviorSuccess bSuccess = new BehaviorSuccess(behaviorInputs, actionOutputs);
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
                    sourceRespName, this._id, behaviorInputs, actionOutputs, exception);
            executionContext.fireEvent(event);
        }

        if (exception != null) {
            if (exception instanceof GeneralException) {
                throw (RuntimeException) exception;
            } else {
                throw new GeneralException(exception);
            }
        }

        // Write outputs back to behavior outputs
        Looper.on(actionOutputs).foreachWithIndex((idx, output) -> behaviorOutputs[idx].set(output.get()));
    }
}
