package uapi.service;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.codegen.IHandlerHelper;

/**
 * Created by xquan on 5/2/2017.
 */
public interface IServiceLifecycleHandlerHelper extends IHandlerHelper {

    String name = "ServiceLifecycleHelper";

    @Override
    default String getName() {
        return name;
    }

    /**
     * Add new method to the init method.
     * The method must be no arguments and return nothing
     *
     * @param   builderContext
     *          The context of compiling time
     * @param   classBuilder
     *          The builder for the class
     * @param   methodNames
     *          The method name
     */
    void addInitMethod(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String... methodNames);
}
