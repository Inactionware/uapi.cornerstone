package uapi.command;

/**
 * Implementation holds command execution result.
 */
public interface ICommandResult {

    /**
     * Indicate whether the command execution is successful or not.
     *
     * @return  True means success otherwise means failure
     */
    boolean successful();

    /**
     * The result message to describe command execution result.
     *
     * @return  The command execution result message
     */
    String message();

    /**
     * The exception of this command execution when the command execution is failed.
     *
     * @return  The exception object or null if no exception for the command execution
     */
    Throwable exception();
}
