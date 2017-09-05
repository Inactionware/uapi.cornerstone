package uapi.command;

public interface ICommandRunner {

    ICommandResult run(String commandLine, IMessageOutput output);
}
