/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

import uapi.command.internal.Command;
import uapi.exception.FileBasedExceptionErrors;
import uapi.exception.IndexedParameters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandErrors extends FileBasedExceptionErrors<CommandException> {

    public static final int CATEGORY   = 0x0106;

    public static final int MULTIPLE_PARENT_COMMAND_FOUND       = 1;
    public static final int DUPLICATED_SUBCOMMAND               = 2;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(MULTIPLE_PARENT_COMMAND_FOUND, MutiParentFound.KEY);
        keyCodeMapping.put(DUPLICATED_SUBCOMMAND, DuplicatedSubCommand.KEY);
    }

    @Override
    protected String getFile(CommandException exception) {
        if (exception.category() == CATEGORY) {
            return "/commandErrors.properties";
        }
        return null;
    }

    @Override
    protected String getKey(CommandException exception) {
        return keyCodeMapping.get(exception.errorCode());
    }

    /**
     * Found multiple parent {} for command {}
     */
    public static final class MutiParentFound extends IndexedParameters<MutiParentFound> {

        private static final String KEY = "MultipleParentCommandFound";

        private ICommandMeta _cmd;

        public MutiParentFound command(ICommandMeta commandMeta) {
            this._cmd = commandMeta;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._cmd.commandId(), this._cmd.parentId() };
        }
    }

    /**
     * The sub-command with name {} was exist in command {}
     */
    public static final class DuplicatedSubCommand extends IndexedParameters<DuplicatedSubCommand> {

        private static final String KEY = "DuplicatedSubCommand";

        private Command _subCmd;
        private Command _parentCmd;

        public DuplicatedSubCommand subcommand(Command command) {
            this._subCmd = command;
            return this;
        }

        public DuplicatedSubCommand parentCommand(Command command) {
            this._parentCmd = command;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._subCmd.name(), this._parentCmd.name() };
        }
    }
}
