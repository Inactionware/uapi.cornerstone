package uapi.command;

/**
 * Implementation of this interface can execution a command by specified command line which contains command name,
 * parameters and options.
 */
public interface ICommandRunner {

    /**
     * Execute command by specified command line.
     *
     * @param   commandLine The command line which contains command name, parameters and options
     * @param   output The message output object
     * @return  The command result
     */
    ICommandResult run(String commandLine, IMessageOutput output);
}
