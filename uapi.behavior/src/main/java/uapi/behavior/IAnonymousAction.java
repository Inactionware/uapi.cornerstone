package uapi.behavior;

/**
 * Created by min on 2017/5/21.
 */
@FunctionalInterface
public interface IAnonymousAction<IT, OT> {

    OT accept(IT input, IExecutionContext executionContext);
}
