package uapi.behavior;

import uapi.common.Functionals;
import uapi.event.IEventFinishCallback;

/**
 * A builder for behavior creation
 */
public interface IBehaviorBuilder {

    /**
     * Set this behavior is traceable or not
     *
     * @param   traceable
     *          Traceable behavior
     * @return  The behavior builder self
     */
    IBehaviorBuilder traceable(boolean traceable);

    /**
     * Set condition for current behavior branch
     *
     * @param   evaluator
     *          The branch condition
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          When the evaluator is already set, see {@link BehaviorErrors.EvaluatorIsSet}
     */
    IBehaviorBuilder when(Functionals.Evaluator evaluator) throws BehaviorException;

    /**
     * Set where is next action/behavior when current branch condition is satisfied.
     *
     * @param   id
     *          The next action/behavior id
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          No action has such id, see {@link BehaviorErrors.ActionNotFound}
     */
    IBehaviorBuilder then(ActionIdentify id) throws BehaviorException;

    /**
     * Set where is next action/behavior when current branch condition is satisfied and specified a label fot it.
     *
     * @param   id
     *          The id of next action/behavior
     * @param   label
     *          The action/behavior label which can be used to navigate to it later
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          No action has such id, see {@link BehaviorErrors.ActionNotFound}
     */
    IBehaviorBuilder then(ActionIdentify id, String label) throws BehaviorException;

    /**
     * Set next an anonymous action when current branch condition is satisfied.
     *
     * @param   action
     *          The anonymous action
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any exception when set action
     */
    IBehaviorBuilder then(IAnonymousAction<?, ?> action) throws BehaviorException;

    /**
     * Set next an anonymous action with specific label when current branch condition is satisfied.
     *
     * @param   action
     *          The anonymous action
     * @param   label
     *          The action/behavior label which can be used to navigate to it later
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any exception when set action
     */
    IBehaviorBuilder then(IAnonymousAction<?, ?> action, String label) throws BehaviorException;

    /**
     * Set next an anonymous action which return nothing when current branch condition is satisfied.
     *
     * @param   call
     *          The anonymous action
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any exception when set the action
     */
    IBehaviorBuilder call(IAnonymousCall<?> call) throws BehaviorException;

    /**
     * Set next an anonymous action which return nothing with specific label when current branch condition is satisfied.
     *
     * @param   call
     *          The anonymous action
     * @param   label
     *          The action/behavior label which can be used to navigate to it later
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any exception when set the action
     */
    IBehaviorBuilder call(IAnonymousCall<?> call, String label) throws BehaviorException;

    /**
     * Invoke the action when the behavior is executed successful
     *
     * @param   action
     *          The action which will be invoked on behavior successful
     * @return  The behavior builder self
     */
    IBehaviorBuilder onSuccess(IAnonymousAction<Object, BehaviorEvent> action);

    /**
     * Invoke the action when the behavior is executed failed
     *
     * @param   action
     *          The action which will be invoked on behavior failed
     * @return  The behavior builder self
     */
    IBehaviorBuilder onFailure(IAnonymousAction<Exception, BehaviorEvent> action);

    IBehaviorBuilder onSuccessEventCallback(IEventFinishCallback callback);

    /**
     * Get navigator which associated with this behavior builder
     *
     * @return  A navigator
     */
    INavigator navigator();

    /**
     * Indi
     */
    IBehavior build();
}
