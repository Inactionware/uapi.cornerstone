package uapi.command.internal;

import uapi.command.CommandErrors;
import uapi.command.CommandException;
import uapi.command.ICommandMeta;
import uapi.common.ArgumentChecker;
import uapi.rx.Looper;

import java.util.ArrayList;
import java.util.List;

public final class Command {

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

    public String commandId() {
        return this._cmdMeta.commandId();
    }

    public String parentId() {
        return this._cmdMeta.parentId();
    }

    public String parentName() {
        return this._parent == null ? "" : this._parent.name();
    }

    void addSubCommand(Command command) {
        Command duplicatedCmd = Looper.on(this._subCmds)
                .map(subCmd -> subCmd.find(command.name()))
                .filter(subCmd -> subCmd != null)
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

    Command find(String commandName) {
        if (name().equals(commandName)) {
            return this;
        }
        return Looper.on(this._subCmds)
                .map(subcmd -> subcmd.find(commandName))
                .filter(subcmd -> subcmd != null)
                .first(null);
    }
}
