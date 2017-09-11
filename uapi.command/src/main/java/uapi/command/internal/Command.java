package uapi.command.internal;

import uapi.command.CommandErrors;
import uapi.command.CommandException;
import uapi.command.ICommandMeta;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Command {

    public static String generateCommandId(Command command) {
        ArgumentChecker.required(command, "command");
        return generateCommandId(command._cmdMeta);
    }

    public static String generateCommandId(ICommandMeta commandMeta) {
        ArgumentChecker.required(commandMeta, "commandMeta");
        return generateCommandId(commandMeta.namespace(), commandMeta.parentPath(), commandMeta.name());
    }

    public static String generateCommandId(
            String namespace,
            String parentPath,
            String name
    ) {
        ArgumentChecker.required(name, "command");
        Map<String, String> namedValues = new HashMap<>();
        namedValues.put("namespace", namespace != null ? namespace : "");
        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
        namedValues.put("parent", parentPath);
        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
        namedValues.put("name", name);
        return StringHelper.makeString("{namespace}{sep}{parent}{sep}{name}", namedValues);
    }

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

    public String name() {
        return this._cmdMeta.name();
    }

    public String namespace() {
        return this._cmdMeta.namespace();
    }

    public boolean hasParent() {
        return this._cmdMeta.hasParent();
    }

    public String parentPath() {
        return this._cmdMeta.parentPath();
    }

    public String commandId() {
        return generateCommandId(this);
    }

    void addSubCommand(Command command) {
        Command duplicatedCmd = Looper.on(this._subCmds)
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

    }

    Command findSubCommand(String commandName) {
        return Looper.on(this._subCmds)
                .filter(subCmd -> subCmd.name().equals(commandName))
                .first(null);
    }
}
