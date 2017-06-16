package uapi.behavior;

/**
 * Created by xquan on 6/16/2017.
 */
public interface IAnonymousCall<T> {

    void accept(T input, IExecutionContext executionContext) throws Exception;
}
