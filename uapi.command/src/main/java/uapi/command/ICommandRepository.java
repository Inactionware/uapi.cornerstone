package uapi.command;

public interface ICommandRepository {

    void register(ICommandMeta commandMeta);

    void unregister(String commandId);

    ICommandMeta find(String commandId);

    ICommandRunner getRunner();
}
