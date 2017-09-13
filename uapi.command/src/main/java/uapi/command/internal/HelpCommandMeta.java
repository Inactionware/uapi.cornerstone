package uapi.command.internal;

import uapi.command.ICommandExecutor;
import uapi.command.ICommandMeta;
import uapi.command.ICommandResult;
import uapi.command.IMessageOutput;
import uapi.common.CollectionHelper;
import uapi.common.StringHelper;
import uapi.rx.Looper;

public class HelpCommandMeta implements ICommandMeta {

    public static final String NAME = "help";

    private final String _parentPath;
    private final ICommand _cmd;

    public HelpCommandMeta(ICommand command) {
        this(ROOT_PATH, command);
    }

    public HelpCommandMeta(
            final String parentPath,
            final ICommand command
    ) {
        this._parentPath = parentPath;
        this._cmd = command;
    }

    @Override
    public String parentPath() {
        return this._parentPath;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public ICommandExecutor newExecutor() {
        return null;
    }

    private final class HelpCommandExecutor implements ICommandExecutor {

        private IMessageOutput _msgOut;

        @Override
        public String commandId() {
            return HelpCommandMeta.this.id();
        }

        @Override
        public void setMessageOutput(IMessageOutput output) {
            this._msgOut = output;
        }

        @Override
        public ICommandResult execute() {
            ICommand command = HelpCommandMeta.this._cmd;
            if (! command.namespace().equals(ICommandMeta.DEFAULT_NAMESPACE)) {
                this._msgOut.output(StringHelper.makeString("Namespace: {}", command.namespace()));
            }
            this._msgOut.output(StringHelper.makeString(
                    "Usage: {} {} {} {} {}",
                    CollectionHelper.asString(command.ancestors(), " "),
                    command.name(),
                    command.availableParameters().length != 0 ? "[parameters]" : "",
                    command.availableOptions().length != 0 ? "[options]" : ""
            ));

            if (command.availableParameters().length != 0) {
                this._msgOut.output(StringHelper.makeString("Available parameters: "));
            }

            if (command.availableParameters().length != 0) {
                this._msgOut.output(StringHelper.makeString("Available options: "));
            }

            ICommand[] subCmds = command.availableSubCommands();
            if (subCmds.length != 0) {
                Looper.on(HelpCommandMeta.this._cmd)
                        .foreach(cmd -> {

                        });
            }

            return null;
        }
    }
}
