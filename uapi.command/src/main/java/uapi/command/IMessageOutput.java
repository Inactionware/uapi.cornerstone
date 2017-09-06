package uapi.command;

/**
 * Implementation of this interface is used to output message during command execution.
 */
public interface IMessageOutput {

    /**
     * Output specific message.
     *
     * @param   message The message
     */
    void output(String message);
}
