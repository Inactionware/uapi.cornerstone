package uapi.command.internal;

import java.util.Iterator;

public interface ICommandIterable {

    Iterator<Command> iterator();
}
