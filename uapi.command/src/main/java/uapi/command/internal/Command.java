package uapi.command.internal;

import uapi.command.*;
import uapi.common.ArgumentChecker;
import uapi.rx.Looper;

import java.util.*;

public final class Command implements ICommand {

    public static String getNamespace(String commandId) {
        ArgumentChecker.required(commandId, "commandId");
        if (commandId.indexOf(ICommandMeta.PATH_SEPARATOR) == 0) {
            return "";
        }
        return commandId.substring(0, commandId.indexOf(ICommandMeta.PATH_SEPARATOR));
    }

    public static String[] getPath(String commandId) {
        ArgumentChecker.required(commandId, "commandId");
        return commandId
                .substring(commandId.indexOf(ICommandMeta.PATH_SEPARATOR) + 1)
                .split(ICommandMeta.PATH_SEPARATOR);
    }

    private final Command _parent;

    private final ICommandMeta _cmdMeta;

    private final List<Command> _subCmds = new ArrayList<>();

    Command(final ICommandMeta commandMeta) {
        this(commandMeta, null);
    }

    Command(final ICommandMeta commandMeta, final Command parent) {
        ArgumentChecker.required(commandMeta, "commandMeta");
        this._parent = parent;
        this._cmdMeta = commandMeta;
    }

    @Override
    public String namespace() {
        return this._cmdMeta.namespace();
    }

    @Override
    public String name() {
        return this._cmdMeta.name();
    }

    @Override
    public String[] ancestors() {
        return this._cmdMeta.ancestors();
    }

    @Override
    public String description() {
        return this._cmdMeta.description();
    }

    @Override
    public IParameterMeta[] availableParameters() {
        return this._cmdMeta.parameterMetas();
    }

    @Override
    public IOptionMeta[] availableOptions() {
        return this._cmdMeta.optionMetas();
    }

    @Override
    public ICommand[] availableSubCommands() {
        return this._subCmds.toArray(new ICommand[this._subCmds.size()]);
    }

    public ICommandMeta meta() {
        return this._cmdMeta;
    }

    public boolean hasParent() {
        return this._cmdMeta.hasParent();
    }

    public String parentPath() {
        return this._cmdMeta.parentPath();
    }

    public String id() {
        return this._cmdMeta.id();
    }

    void addSubCommand(Command command) {
        var duplicatedCmd = Looper.on(this._subCmds)
                .filter(subCmd -> subCmd.name().equals(command.name()))
                .first(null);
        if (duplicatedCmd != null) {
            throw CommandException.builder()
                    .errorCode(CommandErrors.DUPLICATED_SUBCOMMAND)
                    .variables(new CommandErrors.DuplicatedSubCommand()
                        .subcommand(command)
                        .parentCommand(this))
                    .build();
        }
        this._subCmds.add(command);
    }

    void removeSubCommand(String commandName) {
        var command = findSubCommand(commandName);
        this._subCmds.remove(command);
    }

    Command findSubCommand(String commandName) {
        ArgumentChecker.required(commandName, "commandName");
        return Looper.on(this._subCmds)
                .filter(subCmd -> subCmd.name().equals(commandName))
                .first(null);
    }

    ICommandExecutor getExecutor() {
        return this._cmdMeta.newExecutor();
    }
}
