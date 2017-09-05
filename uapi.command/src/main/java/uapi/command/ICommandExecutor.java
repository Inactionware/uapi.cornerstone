package uapi.command;

public interface ICommandExecutor {

    ICommandResult execute();

    void setParameter(String name, Object value);

    void setOption(String name);

    void setMessageOutput(IMessageOutput output);
}
