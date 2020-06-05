package uapi.behavior;

import uapi.common.Functionals;

import java.util.Map;

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
    default IBehaviorBuilder then(ActionIdentify id) throws BehaviorException {
        return this.then(ActionInitializer.instance(id));
    }

//    /**
//     * Set where is next action/behavior when current branch condition is satisfied and specify a label for it.
//     *
//     * @param   id
//     *          The id of next action/behavior
//     * @param   label
//     *          The action/behavior label which can be used to navigate to it later
//     * @param   inputs
//     *          Specified inputs for the action/behavior
//     *          If the input is IOutputReference then the input is a pointer which point to other action's output
//     * @return  The behavior builder self
//     * @throws  BehaviorException
//     *          No action has such id, see {@link BehaviorErrors.ActionNotFound}
//     */
//    IBehaviorBuilder then(ActionIdentify id, String label, Object... inputs) throws BehaviorException;

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
    default IBehaviorBuilder then(Class<?> actionType) throws BehaviorException {
        return this.then(ActionIdentify.toActionId(actionType));
    }

//    /**
//     * Set where is next action/behavior when current branch condition is satisfied and specify a label for it.
//     * The actionType will be converted to default action id which is used to find action in repository.
//     *
//     * @param   actionType
//     *          The action type of next action/behavior
//     * @param   label
//     *          The action/behavior label which can be used to navigate to it late
//     * @param   inputs
//     *          The inputs for the action/behavior
//     * @return  The behavior builder instance
//     * @throws  BehaviorException
//     *          No action in the repository, see {@link BehaviorErrors.ActionNotFound}
//     */
//    IBehaviorBuilder then(Class<?> actionType, String label, Object... inputs) throws BehaviorException;

    IBehaviorBuilder then(ActionInitializer actionInitializer) throws BehaviorException;

//    /**
//     * Set attribute for current action.
//     *
//     * @param   attrs
//     *          The attribute for current action
//     * @return  This behavior build instance
//     * @throws  BehaviorException
//     *          Current action does not support attribute
//     */
//    IBehaviorBuilder attributes(Map<Object, Object> attrs) throws BehaviorException;
//
//    /**
//     * Set input for current action
//     *
//     * @param   inputs
//     *          The inputs for current action
//     * @return  This behavior builder instance
//     * @throws  BehaviorException
//     *          Current action does not support inputs
//     */
//    IBehaviorBuilder inputs(Object... inputs) throws BehaviorException;

    /**
     * Set next an anonymous action which return nothing when current branch condition is satisfied.
     *
     * @param   call
     *          The anonymous action
     * @return  The behavior builder self
     * @throws  BehaviorException
     *          Any cause when set the action
     */
    IBehaviorBuilder call(uapi.behavior.Functionals.AnonymousCall call) throws BehaviorException;

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
    IBehaviorBuilder call(uapi.behavior.Functionals.AnonymousCall call, String label) throws BehaviorException;

    /**
     * Invoke the action when the behavior is executed successful
     *
     * @param   action
     *          The action which will be invoked on behavior successful
     * @return  The behavior builder self
     */
    IBehaviorBuilder onSuccess(uapi.behavior.Functionals.BehaviorSuccessAction action);

    /**
     * Invoke the action when the behavior is executed failed
     *
     * @param   action
     *          The action which will be invoked on behavior failed
     * @return  The behavior builder self
     */
    IBehaviorBuilder onFailure(uapi.behavior.Functionals.BehaviorFailureAction action);

    /**
     * Get navigator which associated with this behavior builder
     *
     * @return  A navigator
     */
    INavigator navigator();

    /**
     * Get reference object which can used to wired action input and output
     *
     * @return  A reference
     */
    IWired wired();

    /**
     * Build behavior
     *
     * @return  Behavior instance
     */
    IBehavior build();
}
