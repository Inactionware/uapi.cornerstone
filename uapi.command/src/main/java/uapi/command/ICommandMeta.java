package uapi.command;

public interface ICommandMeta {

    String parent();

    String name();

    String namespace();

    String description();

    IParameterMeta[] parameterMetas();

    IOptionMeta[] optionMetas();

    ICommandExecutor newExecutor();
}
