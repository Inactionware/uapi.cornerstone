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
import uapi.common.CollectionHelper;
import uapi.common.Multivariate;
import uapi.rx.Looper;
import uapi.service.annotation.Inject;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation for ICommandRepository interface
 */
@Service(ICommandRepository.class)
public class CommandRepository implements ICommandRepository {

    private static final String[] reservedCmdNames = new String[] {
            HelpCommandMeta.NAME
    };

    private final List<Command> _rootCmds = new ArrayList<>();

    private final CommandRunner _cmdRunner = new CommandRunner();

    @Inject
    protected List<ICommandMeta> _commandMetas = new ArrayList<>();

    @OnActivate
    protected void activate() {
        this._commandMetas.sort(Comparator.comparingInt(ICommandMeta::depth));
        Looper.on(this._commandMetas).foreach(commandMeta -> {
            checkReservedCommand(commandMeta.name());
            if (! commandMeta.hasParent()) {
                this._rootCmds.add(new Command(commandMeta));
            } else {
                addSubCommand(commandMeta);
            }
        });
        this._commandMetas.clear();

        // Add root command helper
        this._rootCmds.add(new Command(new HelpCommandMeta(new RootCommand())));
    }

    @Override
    public void register(ICommandMeta commandMeta) {
        ArgumentChecker.required(commandMeta, "commandMeta");
        checkReservedCommand(commandMeta.name());
        if (! commandMeta.hasParent()) {
            this._rootCmds.add(new Command(commandMeta));
        } else {
            addSubCommand(commandMeta);
        }
    }

    @Override
    public void deregister(String commandId) {
        String namespace = Command.getNamespace(commandId);
        String[] path = Command.getPath(commandId);
        Command parentCmd = Looper.on(this._rootCmds)
                .filter(cmd -> cmd.namespace().equals(namespace))
                .filter(cmd -> cmd.name().equals(path[0]))
                .first(null);
        if (parentCmd == null) {
            throw CommandException.builder()
                    .errorCode(CommandErrors.PARENT_COMMAND_NOT_FOUND)
                    .variables(new CommandErrors.ParentCommandNotFound()
                            .parentCommandName(path[0])
                            .thisCommandId(commandId))
                    .build();
        }
        Command command = null;
        for (int i = 1; i < path.length; i++) {
            if (command != null) {
                parentCmd = command;
            }
            command = parentCmd.findSubCommand(path[i]);
            if (command == null) {
                throw CommandException.builder()
                        .errorCode(CommandErrors.COMMAND_NOT_FOUND)
                        .variables(new CommandErrors.CommandNotFound().commandId(commandId))
                        .build();
            }
        }
        if (command == null) {
            this._rootCmds.remove(parentCmd);
        } else {
            parentCmd.removeSubCommand(command.name());
        }
    }

    @Override
    public ICommandRunner getRunner() {
        return this._cmdRunner;
    }

    int commandCount() {
        return this._rootCmds.size();
    }

    private void addSubCommand(ICommandMeta commandMeta) {
        String[] ancestorNames = commandMeta.ancestors();
        Command ancestor = Looper.on(this._rootCmds)
                .filter(cmd -> cmd.name().equals(ancestorNames[0]))
                .first(null);
        if (ancestor == null) {
            throw CommandException.builder()
                    .errorCode(CommandErrors.PARENT_COMMAND_NOT_FOUND)
                    .variables(new CommandErrors.ParentCommandNotFound()
                            .parentCommandName(ancestorNames[0])
                            .thisCommandId(commandMeta.id()))
                    .build();
        }
        for (int i = 1; i < ancestorNames.length; i++) {
            String ancestorName = ancestorNames[i];
            ancestor = ancestor.findSubCommand(ancestorName);
            if (ancestor == null) {
                throw CommandException.builder()
                        .errorCode(CommandErrors.PARENT_COMMAND_NOT_FOUND)
                        .variables(new CommandErrors.ParentCommandNotFound()
                                .parentCommandName(ancestorName)
                                .thisCommandId(commandMeta.id()))
                        .build();
            }
        }
        Command command = new Command(commandMeta, ancestor);
        ancestor.addSubCommand(command);

        // Add help command
        command.addSubCommand(new Command(new HelpCommandMeta(ancestor)));
    }

    private void checkReservedCommand(String cmdName) {
        if (CollectionHelper.isContains(reservedCmdNames, cmdName)) {
            throw CommandException.builder()
                    .errorCode(CommandErrors.RESERVED_COMMAND_NAME)
                    .variables(new CommandErrors.ReservedCommandName().commandName(cmdName))
                    .build();
        }
    }

    /**
     * The root command is only used to make help command can output information
     */
    private final class RootCommand implements ICommand {

        @Override
        public String namespace() {
            return ICommandMeta.DEFAULT_NAMESPACE;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public String[] ancestors() {
            return new String[0];
        }

        @Override
        public String description() {
            return null;
        }

        @Override
        public ICommand[] availableSubCommands() {
            return CommandRepository.this._rootCmds.toArray(new ICommand[CommandRepository.this._rootCmds.size()]);
        }

        @Override
        public IParameterMeta[] availableParameters() {
            return new IParameterMeta[0];
        }

        @Override
        public IOptionMeta[] availableOptions() {
            return new IOptionMeta[0];
        }
    }

    /**
     * An implementation for command runner interface
     */
    private final class CommandRunner implements ICommandRunner {

        @Override
        public CommandResult run(
                final String commandLine,
                final IMessageOutput output
        ) throws CommandException {
            ArgumentChecker.required(commandLine, "commandLine");
            String[] cmdParamOpts = commandLine.split(" ");
            int idxNs = cmdParamOpts[0].indexOf(ICommandMeta.PATH_SEPARATOR);
            String namespace;
            String cmdName;
            if (idxNs >= 0) {
                namespace = cmdParamOpts[0].substring(0, idxNs);
                cmdName = cmdParamOpts[0].substring(idxNs + 1, cmdParamOpts[0].length());
            } else {
                namespace = ICommandMeta.DEFAULT_NAMESPACE;
                cmdName = cmdParamOpts[0];
            }

            // Find out root command
            Command command = Looper.on(CommandRepository.this._rootCmds)
                    .filter(cmd -> cmd.namespace().equals(namespace))
                    .filter(cmd -> cmd.name().equals(cmdName))
                    .first(null);
            if (command == null) {
                throw CommandException.builder()
                        .errorCode(CommandErrors.COMMAND_NOT_FOUND)
                        .variables(new CommandErrors.CommandNotFound().commandId(cmdName))
                        .build();
            }

            // Find out command and command's parameter and option list
            Multivariate cmdVar = new Multivariate(2);
            cmdVar.put(0, command);      // command object
            cmdVar.put(1, true);   // does need to find sub command
            List<String> paramOpts = new ArrayList<>();
            Looper.on(cmdParamOpts).skip(1).foreach(cmdParamOpt -> {
                Command cmd = cmdVar.get(0);
                if (cmdVar.get(1)) {
                    Command subCmd = cmd.findSubCommand(cmdParamOpt);
                    if (subCmd != null) {
                        cmdVar.put(0, subCmd);
                        return;
                    }
                }
                paramOpts.add(cmdParamOpt);
            });

            // Set command parameter and options
            Command cmd = cmdVar.get(0);
            Multivariate optParamVar = new Multivariate(2);  // 0 -> option name; 1 -> parameter index
            optParamVar.put(1, 0);
            ICommandMeta cmdMeta = cmd.meta();
            ICommandExecutor cmdExec = cmd.getExecutor();
            IParameterMeta[] paramMetas = cmdMeta.parameterMetas();
            IOptionMeta[] optMetas = cmdMeta.optionMetas();
            Looper.on(paramOpts).foreach(paramOpt -> {
                if (paramOpt.indexOf(IOptionMeta.LONG_PREFIX) == 0) {
                    // Handle long option
                    if (optParamVar.hasValue(0)) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.OPTION_NEEDS_VALUE)
                                .variables(new CommandErrors.OptionNeedsValue()
                                        .optionName(optParamVar.get(0))
                                        .commandLine(commandLine))
                                .build();
                    }
                    String optName = paramOpt.substring(2);
                    if (ArgumentChecker.isEmpty(optName)) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.EMPTY_OPTION_NAME)
                                .variables(new CommandErrors.EmptyOptionName().commandLine(commandLine))
                                .build();
                    }
                    IOptionMeta matchedOpt = Looper.on(optMetas).filter(opt -> opt.name().equals(optName)).first(null);
                    if (matchedOpt == null) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.UNSUPPORTED_OPTION)
                                .variables(new CommandErrors.UnsupportedOption()
                                        .optionName(optName)
                                        .command(commandLine))
                                .build();
                    }
                    if (matchedOpt.type() == OptionType.Boolean) {
                        cmdExec.setOption(optName);
                    } else {
                        optParamVar.put(0, optName);
                    }
                } else if (paramOpt.indexOf(IOptionMeta.SHORT_PREFIX) == 0) {
                    // Handle short option
                    if (optParamVar.hasValue(0)) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.OPTION_NEEDS_VALUE)
                                .variables(new CommandErrors.OptionNeedsValue()
                                        .optionName(optParamVar.get(0))
                                        .commandLine(commandLine))
                                .build();
                    }
                    String optStr = paramOpt.substring(1);
                    if (ArgumentChecker.isEmpty(optStr)) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.EMPTY_OPTION_NAME)
                                .variables(new CommandErrors.EmptyOptionName().commandLine(commandLine))
                                .build();
                    }
                    for (char opt : optStr.toCharArray()) {
                        IOptionMeta matchedOpt = Looper.on(optMetas)
                                .filter(optMeta -> optMeta.shortName() == opt)
                                .first(null);
                        if (matchedOpt == null) {
                            throw CommandException.builder()
                                    .errorCode(CommandErrors.UNSUPPORTED_OPTION)
                                    .variables(new CommandErrors.UnsupportedOption()
                                            .optionName(String.valueOf(opt))
                                            .command(commandLine))
                                    .build();
                        }
                        if (matchedOpt.type() == OptionType.String) {
                            if (optStr.length() > 1) {
                                throw CommandException.builder()
                                        .errorCode(CommandErrors.SET_ARGUMENT_ON_COMBINED_SHORT_OPTION)
                                        .variables(new CommandErrors.SetArgumentOnCombinedShortOption()
                                                .combinedOptions(optStr)
                                                .commandLine(commandLine))
                                        .build();
                            }
                            optParamVar.put(0, matchedOpt.name());
                        } else {
                            cmdExec.setOption(matchedOpt.name());
                        }
                    }
                } else if (optParamVar.hasValue(0)) {
                    // Handle option argument
                    cmdExec.setOption(optParamVar.get(0), paramOpt);
                    optParamVar.put(0, null);
                } else {
                    // Handle parameter
                    int paramIdx = optParamVar.get(1);
                    if (paramIdx >= paramMetas.length) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.PARAM_OUT_OF_INDEX)
                                .variables(new CommandErrors.ParameterOutOfIndex()
                                        .index(paramIdx)
                                        .parameter(paramOpt)
                                        .commandLine(commandLine))
                                .build();
                    }
                    IParameterMeta paramMeta = paramMetas[paramIdx];
                    cmdExec.setParameter(paramMeta.name(), paramOpt);
                    optParamVar.put(1, paramIdx + 1);
                }
            });

            // Check required parameter
            int paramIdx = optParamVar.get(1);
            if (paramIdx < paramMetas.length) {
                for (int i = paramIdx; i < paramMetas.length; i++) {
                    if (paramMetas[i].required()) {
                        throw CommandException.builder()
                                .errorCode(CommandErrors.MISSING_REQUIRED_PARAMETER)
                                .variables(new CommandErrors.MissingRequiredParameter()
                                        .parameterName(paramMetas[i].name())
                                        .commandLine(commandLine))
                                .build();
                    }
                }
            }

            cmdExec.setMessageOutput(output);

            return cmdExec.execute();
        }
    }
}
