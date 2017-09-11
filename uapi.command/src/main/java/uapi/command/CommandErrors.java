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

    public static final int PARENT_COMMAND_NOT_FOUND            = 1;
    public static final int DUPLICATED_SUBCOMMAND               = 2;
    public static final int COMMAND_NOT_FOUND                   = 3;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(PARENT_COMMAND_NOT_FOUND, ParentCommandNotFound.KEY);
        keyCodeMapping.put(DUPLICATED_SUBCOMMAND, DuplicatedSubCommand.KEY);
        keyCodeMapping.put(COMMAND_NOT_FOUND, CommandNotFound.KEY);
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
     * No parent command named {} for command {}
     */
    public static final class ParentCommandNotFound extends IndexedParameters<ParentCommandNotFound> {

        private static final String KEY = "ParentCommandNotFound";

        private String _parentCmd;
        private String _thisCmdId;

        public ParentCommandNotFound parentCommandName(String commandName) {
            this._parentCmd = commandName;
            return this;
        }

        public ParentCommandNotFound thisCommandId(String commandId) {
            this._thisCmdId = commandId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._parentCmd, this._thisCmdId };
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

    /**
     * Command was not found - {}
     */
    public static final class CommandNotFound extends IndexedParameters<CommandNotFound> {

        public static final String KEY = "CommandNotFound";

        private String _cmdId;

        public CommandNotFound commandId(String commandId) {
            this._cmdId = commandId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._cmdId };
        }
    }
}
