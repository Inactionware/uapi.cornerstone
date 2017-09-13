package uapi.command.internal;

import uapi.command.IOptionMeta;
import uapi.command.IParameterMeta;

import java.util.Iterator;

public interface ICommand {

    String namespace();

    String name();

    String[] ancestors();

    String description();

    ICommand[] availableSubCommands();

    IParameterMeta[] availableParameters();

    IOptionMeta[] availableOptions();
}
