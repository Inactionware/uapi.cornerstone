package uapi.service.annotation.handler;

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
     * Add new method to the activation method.
     * The method must be no arguments and return nothing
     *
     * @param   builderContext
     *          The context of compiling time
     * @param   classBuilder
     *          The builder for the class
     * @param   methodNames
     *          The method name
     */
    void addActivateMethod(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String... methodNames);

    /**
     * Add new method to the injection method.
     * The method must be no arguments and return nothing
     *
     * @param   builderContext
     *          The context of compiling time
     * @param   classBuilder
     *          The builder for the class
     * @param   methodName
     *          The method name
     * @param   serviceId
     *          The identify of service
     * @param   serviceType
     *          The type of service
     */
    void addInjectMethod(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String methodName,
            final String serviceId,
            final String serviceType);

    /**
     * Add new method to the deactivation method.
     * The method must be no arguments and return nothing
     *
     * @param   builderContext
     *          The context of compiling time
     * @param   classBuilder
     *          The builder for the class
     * @param   methodNames
     *          The method name
     */
    void addDeactivateMethod(
            final IBuilderContext builderContext,
            final ClassMeta.Builder classBuilder,
            final String... methodNames);
}
