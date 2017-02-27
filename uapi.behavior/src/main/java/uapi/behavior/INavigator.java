package uapi.behavior;

/**
 * A navigator can navigate to any labeled action/behavior in one behavior
 */
public interface INavigator {

    /**
     * Move current cursor to starting action
     *
     * @return  The associated behavior builder
     */
    IBehaviorBuilder moveToStarting();

    /**
     * Move current cursor to previous action
     *
     * @return  The associated behavior builder
     */
//    IBehaviorBuilder moveToPrevious();

    /**
     * Move current cursor to specific labeled action.
     *
     * @param   label
     *          The action/behavior label in this behavior
     * @return  The associated behavior builder
     * @throws  BehaviorException
     *          If the labeled action was not found, see {@link BehaviorErrors.NoActionWithLabel}
     */
    IBehaviorBuilder moveTo(String label) throws BehaviorException;
}
