package uapi.command;

/**
 * Implementation of this interface holds all command information.
 */
public interface ICommandRepository {

    /**
     * Register new command meta.
     *
     * @param   commandMeta The command meta
     */
    void register(ICommandMeta commandMeta);

    /**
     * Unregister command by specified command id.
     *
     * @param   commandId The command id which is consist by command namespace and command name
     */
    void unregister(String commandId);

    /**
     * Find out the command by specified command id.
     *
     * @param   commandId The command id which is consist by command namespace and command name
     * @return  The command meta object or null if no command is matched the command id.
     */
    ICommandMeta find(String commandId);

    /**
     * Return the command runner object.
     *
     * @return  The command runner
     */
    ICommandRunner getRunner();
}
