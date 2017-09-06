package uapi.command;

/**
 * The class holds command parameter meta information.
 */
public interface IParameterMeta {

    /**
     * The name of the command parameter.
     *
     * @return  The name of command parameter
     */
    String name();

    /**
     * Is the parameter is required for the command.
     *
     * @return  True means the parameter is required to the command otherwise it is optional
     */
    boolean required();

    /**
     * Description of the command parameter.
     *
     * @return  Description of the command parameter
     */
    String description();
}
