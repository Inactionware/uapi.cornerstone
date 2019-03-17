package uapi.behavior;

/**
 * The interface is used add anonymous action to Behavior
 */
public interface IAnonymousCall {

    void accept(IExecutionContext executionContext) throws Exception;
}
