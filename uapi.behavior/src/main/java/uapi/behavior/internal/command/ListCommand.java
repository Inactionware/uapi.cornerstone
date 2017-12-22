package uapi.behavior.internal.command;

import uapi.behavior.internal.ResponsibleRegistry;
import uapi.command.annotation.Command;
import uapi.command.annotation.Option;
import uapi.command.annotation.Parameter;
import uapi.command.annotation.Run;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;

@Service
@Command(
        parent = BehaviorCommand.class,
        name = "list",
        description = "List all available behavior commands")
public class ListCommand {

    @Inject
    protected ResponsibleRegistry _respReg;

    @Parameter(index=0, name="responsible", description="Show all available responsible")
    protected String _responsible;

    @Option(name="behavior", shortName='b', description="Show behaviors under specific responsible")
    protected boolean _listBehaviors;

    @Run
    public boolean run() {
        return true;
    }
}
