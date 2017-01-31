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
     */
    IResponsible register(String name);

    /**
     * Unregister responsible by specific name.
     *
     * @param   name
     *          The responsible name
     */
    void unregister(String name);
}
