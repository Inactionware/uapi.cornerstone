/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.command.internal;

import uapi.command.*;
import uapi.common.ArgumentChecker;
import uapi.rx.Looper;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation for ICommandRepository interface
 */
@Service(ICommandRunner.class)
public class CommandRepository implements ICommandRepository {

    private final List<Command> _rootCmds = new ArrayList<>();

    private final List<ICommandMeta> _unresolvedCmds = new ArrayList<>();

    @Inject
    protected void addCommand(final ICommandMeta commandMeta) {
        ArgumentChecker.required(commandMeta, "commandMeta");
        if (! commandMeta.hasParent()) {
            this._rootCmds.add(new Command(commandMeta));
        } else {
            String[] ancestorNames = commandMeta.ancestors();
            Command ancestor = Looper.on(this._rootCmds)
                    .filter(cmd -> cmd.name().equals(ancestorNames[0]))
                    .first();
            if (ancestor == null) {
                this._unresolvedCmds.add(commandMeta);
                return;
//                throw CommandException.builder()
//                        .errorCode(CommandErrors.PARENT_COMMAND_NOT_FOUND)
//                        .variables(new CommandErrors.ParentCommandNotFound().command(commandMeta))
//                        .build();
            }
            for (int i = 1; i < ancestorNames.length; i++) {
                String ancestorName = ancestorNames[i];
                ancestor = ancestor.findSubCommand(ancestorName);
                if (ancestor == null) {
                    this._unresolvedCmds.add(commandMeta);
                    return;
                }
            }
            Command command = new Command(commandMeta, ancestor);
            ancestor.addSubCommand(command);
        }
    }

    @Override
    public void register(ICommandMeta commandMeta) {

    }

    @Override
    public void deregister(String commandId) {

    }

    @Override
    public ICommandMeta find(String commandId) {
        return null;
    }

    @Override
    public ICommandRunner getRunner() {
        return null;
    }

    /**
     * An implementation for command runner interface
     */
    private final class CommandRunner implements ICommandRunner {

        @Override
        public ICommandResult run(String commandLine, IMessageOutput output) {
            return null;
        }
    }
}
