package uapi.behavior;

/**
 * A registry for responsible
 */
public interface IResponsibleRegistry {

    /**
     * Register and create new responsible by specific name.
     * If there is a responsible with the name exist in the registry then the existing responsible will return
     *
     * @param   name
     *          The responsible name
     * @return  The responsible instance
     * @throws  BehaviorException
     *          Register name which exist in the registry, see {@link BehaviorErrors.DuplicatedResponsibleName}
     */
    IResponsible register(String name) throws BehaviorException;

    /**
     * Unregister responsible by specific name.
     *
     * @param   name
     *          The responsible name
     */
    void unregister(String name);

    /**
     * Return count of registered responsible
     *
     * @return  Registered responsible count
     */
    int responsibleCount();
}
