package uapi.behavior;

/**
 * A navigator can navigate to any labeled action/behavior in one behavior
 */
public interface INavigator {

    /**
     * Move current cursor to specific label.
     *
     * @param   label
     *          The action/behavior label in this behavior
     * @return  The associated behavior builder
     */
    IBehaviorBuilder moveCursor(String label);
}
