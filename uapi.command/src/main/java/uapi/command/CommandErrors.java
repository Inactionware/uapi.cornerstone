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
    public static final int OPTION_NEEDS_VALUE                  = 4;
    public static final int EMPTY_OPTION_NAME                   = 5;
    public static final int UNSUPPORTED_OPTION                  = 6;
    public static final int PARAM_OUT_OF_INDEX                  = 7;
    public static final int MISSING_REQUIRED_PARAMETER          = 8;
    public static final int UNSUPPORTED_PARAMETER               = 9;
    public static final int RESERVED_COMMAND_NAME               = 10;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(PARENT_COMMAND_NOT_FOUND, ParentCommandNotFound.class.getName());
        keyCodeMapping.put(DUPLICATED_SUBCOMMAND, DuplicatedSubCommand.class.getName());
        keyCodeMapping.put(COMMAND_NOT_FOUND, CommandNotFound.class.getName());
        keyCodeMapping.put(OPTION_NEEDS_VALUE, OptionNeedsValue.class.getName());
        keyCodeMapping.put(EMPTY_OPTION_NAME, EmptyOptionName.class.getName());
        keyCodeMapping.put(UNSUPPORTED_OPTION, UnsupportedOption.class.getName());
        keyCodeMapping.put(PARAM_OUT_OF_INDEX, ParameterOutOfIndex.class.getName());
        keyCodeMapping.put(MISSING_REQUIRED_PARAMETER, MissingRequiredParameter.class.getName());
        keyCodeMapping.put(UNSUPPORTED_PARAMETER, UnsupportedParameter.class.getName());
        keyCodeMapping.put(RESERVED_COMMAND_NAME, ReservedCommandName.class.getName());
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

    /**
     * Invalid command option which needs value - {}, command line: {}
     */
    public static final class OptionNeedsValue extends IndexedParameters<OptionNeedsValue> {

        private String _optName;
        private String _cmdLine;

        public OptionNeedsValue optionName(String name) {
            this._optName = name;
            return this;
        }

        public OptionNeedsValue commandLine(String commandLine) {
            this._cmdLine = commandLine;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._optName, this._cmdLine };
        }
    }

    /**
     * Invalid command option which has empty name, command line: {}
     */
    public static final class EmptyOptionName extends IndexedParameters<EmptyOptionName> {

        private String _cmdLine;

        public EmptyOptionName commandLine(String commandLine) {
            this._cmdLine = commandLine;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._cmdLine };
        }
    }

    /**
     * Invalid command option which is not supported - {}, command: {}
     */
    public static final class UnsupportedOption extends IndexedParameters<UnsupportedOption> {

        private String _optName;
        private String _cmdLine;

        public UnsupportedOption optionName(String name) {
            this._optName = name;
            return this;
        }

        public UnsupportedOption command(String commandLine) {
            this._cmdLine = commandLine;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._optName, this._cmdLine };
        }
    }

    /**
     * Invalid command parameter which is out of index - {}, parameter: {}, command line: {}
     */
    public static final class ParameterOutOfIndex extends IndexedParameters<ParameterOutOfIndex> {

        private int _idx;
        private String _param;
        private String _cmdLine;

        public ParameterOutOfIndex index(int index) {
            this._idx = index;
            return this;
        }

        public ParameterOutOfIndex parameter(String parameter) {
            this._param = parameter;
            return this;
        }

        public ParameterOutOfIndex commandLine(String commandLine) {
            this._cmdLine = commandLine;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._idx, this._param, this._cmdLine };
        }
    }

    /**
     * The required parameter was missing - {}, command line: {}
     */
    public static final class MissingRequiredParameter extends IndexedParameters<MissingRequiredParameter> {

        private String _paramName;
        private String _cmdLine;

        public MissingRequiredParameter parameterName(String name) {
            this._paramName = name;
            return this;
        }

        public MissingRequiredParameter commandLine(String commandLine) {
            this._cmdLine = commandLine;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._paramName, this._cmdLine };
        }
    }

    /**
     * Invalid command parameter which is not supported - {}, command: {}
     */
    public static final class UnsupportedParameter extends IndexedParameters<UnsupportedParameter> {

        private String _paramName;
        private String _cmdId;

        public UnsupportedParameter parameterName(String name) {
            this._paramName = name;
            return this;
        }

        public UnsupportedParameter commandId(String id) {
            this._cmdId = id;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._paramName, this._cmdId };
        }
    }

    /**
     * The command name is reserved for internal usage - {}
     */
    public static final class ReservedCommandName extends IndexedParameters<ReservedCommandName> {

        private String _cmdName;

        public ReservedCommandName commandName(String name) {
            this._cmdName = name;
            return this;
        }

        public Object[] get() {
            return new Object[] { this._cmdName };
        }
    }
}
