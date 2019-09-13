package uapi.command.internal;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;

import javax.lang.model.element.Element;

public class CommandBuilderUtil {

    public static ClassMeta.Builder getCommandMetaBuilder(
            final Element classElement,
            final IBuilderContext builderContext
    ) {
        var pkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        var className = classElement.getSimpleName().toString();
        var execClassName = className + "_Meta_Generated";
        var cmdMetaBuilder = builderContext.findClassBuilder(pkgName, execClassName, false);
        if (cmdMetaBuilder == null) {
            cmdMetaBuilder = builderContext.newClassBuilder(pkgName, execClassName);
        }
        return cmdMetaBuilder;
    }

    public static ClassMeta.Builder getCommandExecutorBuilder(
            final Element classElement,
            final IBuilderContext builderContext
    ) {
        var cmdPkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        var cmdClassName = classElement.getSimpleName().toString();
        var execClassName = cmdClassName + "_Executor_Generated";
        var cmdExecBuilder = builderContext.findClassBuilder(cmdPkgName, execClassName, false);
        if (cmdExecBuilder == null) {
            cmdExecBuilder = builderContext.newClassBuilder(cmdPkgName, execClassName);
        }
        return cmdExecBuilder;
    }

    private CommandBuilderUtil() { }
}
