package uapi.command.internal;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;

import javax.lang.model.element.Element;

public class CommandBuilderUtil {

    public static ClassMeta.Builder getCommandMetaBuilder(
            final Element classElement,
            final IBuilderContext builderContext
    ) {
        String pkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        String className = classElement.getSimpleName().toString();
        String execClassName = className + "_Meta_Generated";
        ClassMeta.Builder cmdMetaBuilder = builderContext.findClassBuilder(pkgName, execClassName, false);
        if (cmdMetaBuilder == null) {
            cmdMetaBuilder = builderContext.newClassBuilder(pkgName, execClassName);
        }
        return cmdMetaBuilder;
    }

    public static ClassMeta.Builder getCommandExecutorBuilder(
            final Element classElement,
            final IBuilderContext builderContext
    ) {
        String cmdPkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        String cmdClassName = classElement.getSimpleName().toString();
        String execClassName = cmdClassName + "_Executor_Generated";
        ClassMeta.Builder cmdExecBuilder = builderContext.findClassBuilder(cmdPkgName, execClassName, false);
        if (cmdExecBuilder == null) {
            cmdExecBuilder = builderContext.newClassBuilder(cmdPkgName, execClassName);
        }
        return cmdExecBuilder;
    }
}
