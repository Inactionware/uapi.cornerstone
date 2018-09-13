package uapi.behavior;

/**
 * The action is used to intercept other action.
 *
 * @param   <I> The action input and output type
 */
public interface IInterceptor<I> extends IAction<I, I> {

    /**
     * Return the interceptor output type
     *
     * @return  The output type
     */
    default Class<I> outputType() {
        return inputType();
    }
}
