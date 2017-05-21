package uapi.behavior;

/**
 * Created by min on 2017/5/21.
 */
public interface IAnonymousAction {

    @FunctionalInterface
    interface IContextIgnorant<IT, OT> extends IAnonymousAction {
        OT accept(IT input);
    }

    @FunctionalInterface
    public interface IContextAware<IT, OT> extends IAnonymousAction {
        OT accept(IT input, IExecutionContext executionContext);
    }
}
