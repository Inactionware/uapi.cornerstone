package uapi.behavior;

import uapi.common.Functionals;

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
     * Set where is next action/behavior when current branch condition is satisfied and specify a label for it.
     *
     * @param   id
     *          The id of next action/behavior
     * @param   label
     *          The action/behavior label which can be used to navigate to it later
     * @param   inputs
     *          Specified inputs for the action/behavior
     *          If the input is ActionInputReference then the input is a pointer which point to other action's input
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          No action has such id, see {@link BehaviorErrors.ActionNotFound}
     */
    IBehaviorBuilder then(ActionIdentify id, String label, Object... inputs) throws BehaviorException;

    /**
     * Set where is next action/behavior when current branch condition is satisfied.
     * The actionType will be converted to default action id which is used to find action in repository.
     *
     * @param   actionType
     *          The action type of next action/behavior
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          No such action in the repository, see {@link BehaviorErrors.ActionNotFound}
     */
    IBehaviorBuilder then(Class<? extends IAction> actionType) throws BehaviorException;

    /**
     * Set where is next action/behavior when current branch condition is satisfied and specify a label for it.
     * The actionType will be converted to default action id which is used to find action in repository.
     *
     * @param   actionType
     *          The action type of next action/behavior
     * @param   label
     *          The action/behavior label which can be used to navigate to it late
     * @param   inputs
     *          The inputs for the action/behavior
     * @return  The behavior builder instance
     * @throws  BehaviorException
     *          No action in the repository, see {@link BehaviorErrors.ActionNotFound}
     */
    IBehaviorBuilder then(Class<? extends IAction> actionType, String label, Object... inputs) throws BehaviorException;

    /**
     * Set next an anonymous action which return nothing when current branch condition is satisfied.
     *
     * @param   call
     *          The anonymous action
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any cause when set the action
     */
    IBehaviorBuilder call(IAnonymousCall call) throws BehaviorException;

    /**
     * Set next an anonymous action which return nothing with specific label when current branch condition is satisfied.
     *
     * @param   call
     *          The anonymous action
     * @param   label
     *          The action/behavior label which can be used to navigate to it later
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any cause when set the action
     */
    IBehaviorBuilder call(IAnonymousCall call, String label) throws BehaviorException;

    /**
     * Invoke the action when the behavior is executed successful
     *
     * @param   action
     *          The action which will be invoked on behavior successful
     * @return  The behavior builder self
     */
    IBehaviorBuilder onSuccess(IBehaviorSuccessCall action);

    /**
     * Invoke the action when the behavior is executed failed
     *
     * @param   action
     *          The action which will be invoked on behavior failed
     * @return  The behavior builder self
     */
    IBehaviorBuilder onFailure(IBehaviorFailureCall action);

    /**
     * Get navigator which associated with this behavior builder
     *
     * @return  A navigator
     */
    INavigator navigator();

    /**
     * Build behavior
     *
     * @return  Behavior instance
     */
    IBehavior build();
}
