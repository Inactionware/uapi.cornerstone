package uapi.behavior;

import uapi.common.Functionals;

/**
 * A builder for behavior creation
 */
public interface IBehaviorBuilder {

//    /**
//     * Set behavior name
//     *
//     * @param   name
//     *          The behavior name
//     * @return  The behavior builder self
//     */
//    IBehaviorBuilder name(String name);

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
     * Ser where is next action/behavior when current branch condition is satisfied and specified a label fot it.
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
