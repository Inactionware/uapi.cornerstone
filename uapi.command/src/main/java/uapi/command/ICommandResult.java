package uapi.command;

public interface ICommandResult {

    boolean successful();

    String message();

    String errorMessage();

    Throwable exception();
}
