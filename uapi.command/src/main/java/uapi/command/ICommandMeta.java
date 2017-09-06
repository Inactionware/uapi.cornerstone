package uapi.command;

/**
 * The implementation of this interface holds meta information of a command.
 */
public interface ICommandMeta {

    /**
     * The the parent command of this command.
     * If the parent is null means no parent.
     *
     * @return  The parent command of this command
     */
    String parent();

    /**
     * The name of this command.
     *
     * @return  Name of this command
     */
    String name();

    /**
     * The namespace of this command.
     *
     * @return  Namespace of this command
     */
    String namespace();

    /**
     * Description of this command.
     *
     * @return  Description of this command
     */
    String description();

    /**
     * List of parameter meta of this command.
     *
     * @return  Return parameter meta list
     */
    IParameterMeta[] parameterMetas();

    /**
     * List of option meta of this command.
     *
     * @return  Return option meta list
     */
    IOptionMeta[] optionMetas();

    /**
     * Create new command executor.
     *
     * @return  The new command executor
     */
    ICommandExecutor newExecutor();
}
