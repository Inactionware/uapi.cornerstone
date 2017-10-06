package uapi.command.internal;

import com.google.auto.common.MoreElements;
import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.ICommandMeta;
import uapi.command.annotation.Command;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandParser {

    private static final String TEMPLATE_NAME           = "template/name_method.ftl";
    private static final String TEMPLATE_NAMESPACE      = "template/namespace_method.ftl";
    private static final String TEMPLATE_PARENT_PATH    = "template/parentPath_method.ftl";
    private static final String TEMPLATE_DESCRIPTION    = "template/description_method.ftl";

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(classElement -> {
            if (classElement.getKind() != ElementKind.CLASS) {
                throw new GeneralException(
                        "The element {} must be a class element", classElement.getSimpleName().toString()
                );
            }
            builderContext.checkAnnotations(classElement, Service.class);
            builderContext.checkModifiers(classElement, Command.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            Command command = classElement.getAnnotation(Command.class);
            String cmdNs = command.namespace();
            String cmdName = command.name();
            String cmdDesc = command.description();
            String cmdParentPath = getParentCommandPath(cmdNs, classElement, true);

            // Setup model
            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            CommandModel cmdModel = classBuilder.getTransience(CommandHandler.CMD_MODEL);
            if (cmdModel == null) {
                cmdModel = new CommandModel();
                classBuilder.putTransience(CommandHandler.CMD_MODEL, cmdModel);
            }
            cmdModel.name = cmdName;
            cmdModel.namespace = cmdNs;
            cmdModel.description = cmdDesc;
            cmdModel.parentPath = cmdParentPath;
            Map<String, CommandModel> tmpModel = new HashMap<>();
            tmpModel.put("command", cmdModel);

            // Setup template
            Template tmpName = builderContext.loadTemplate(TEMPLATE_NAME);
            Template tmpNamespace = builderContext.loadTemplate(TEMPLATE_NAMESPACE);
            Template tmpParentPath = builderContext.loadTemplate(TEMPLATE_PARENT_PATH);
            Template tmpDesc = builderContext.loadTemplate(TEMPLATE_DESCRIPTION);

            // Construct class builder
            classBuilder.addImplement(ICommandMeta.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("name")
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tmpModel)
                                    .setTemplate(tmpName)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("namespace")
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tmpModel)
                                    .setTemplate(tmpNamespace)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("description")
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tmpModel)
                                    .setTemplate(tmpDesc)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("parentPath")
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tmpModel)
                                    .setTemplate(tmpParentPath)));
        });
    }

    private String getParentCommandPath(
            final String namespace,
            final Element classElement,
            final boolean isThisCommand
    ) {
        Command thisCommand = classElement.getAnnotation(Command.class);
        String thisCmdName = thisCommand.name();
        AnnotationMirror cmdMirror = MoreElements.getAnnotationMirror(classElement, Command.class).get();
        Element parentType = Looper.on(cmdMirror.getElementValues().entrySet())
                .filter(entry -> "value".equals(entry.getKey().getSimpleName().toString()))
                .map(Map.Entry::getValue)
                .map(annoValue -> (DeclaredType) annoValue.getValue())
                .map(DeclaredType::asElement)
                .first(null);
        if (void.class.getCanonicalName().equals(parentType.getSimpleName().toString())) {
            return ICommandMeta.ROOT_PATH;
        }
        Command parentCommand = parentType.getAnnotation(Command.class);
        if (parentCommand == null) {
            throw new GeneralException(
                    "No Command annotation was declared on class - {}", classElement.getSimpleName().toString());
        }
        if (! namespace.equals(parentCommand.namespace())) {
            throw new GeneralException(
                    "The namespace of parent command does not equals to {} - {}", namespace, parentCommand.namespace());
        }
        String parentPath = getParentCommandPath(namespace, parentType, false);
        if (isThisCommand) {
            return parentPath;
        }
        if (ICommandMeta.ROOT_PATH.equals(parentPath)) {
            return ICommandMeta.PATH_SEPARATOR + thisCmdName;
        } else {
            return parentPath + ICommandMeta.PATH_SEPARATOR + thisCmdName;
        }
    }
}
