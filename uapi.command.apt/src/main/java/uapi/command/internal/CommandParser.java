package uapi.command.internal;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.codegen.ParameterMeta;
import uapi.command.ICommandExecutor;
import uapi.command.ICommandMeta;
import uapi.command.annotation.Command;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IServiceHandlerHelper;

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
    private static final String TEMPLATE_NEW_EXEC       = "template/newExecutor_method.ftl";
    private static final String TEMP_CMD_ID             = "template/commandId_method.ftl";

    static final String FIELD_USER_CMD                  = "_userCmd";
    static final String FIELD_CMD_META                  = "_cmdMeta";

    public static final String VAR_CMD_META_FIELD       = "commandMetaFieldName";

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
            builderContext.checkModifiers(classElement, Command.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            Command command = classElement.getAnnotation(Command.class);
            String cmdNs = command.namespace();
            String cmdName = command.name();
            String cmdDesc = command.description();
            String cmdParentPath = getParentCommandPath(cmdNs, classElement, true);

            // Initial command meta class builder
            ClassMeta.Builder metaBuilder = CommandBuilderUtil.getCommandMetaBuilder(classElement, builderContext);
            CommandModel cmdModel = new CommandModel();
            metaBuilder.putTransience(CommandHandler.CMD_MODEL, cmdModel);
            IServiceHandlerHelper svcHelper = (IServiceHandlerHelper) builderContext.getHelper(IServiceHandlerHelper.name);
            svcHelper.addServiceId(metaBuilder, metaBuilder.getGeneratedClassName());

            // Initial user command class builder
            ClassMeta.Builder cmdBuilder = builderContext.findClassBuilder(classElement);
            cmdBuilder.putTransience(CommandHandler.CMD_MODEL, cmdModel);

            // Initial command executor class builder
            Template tempCmdId = builderContext.loadTemplate(TEMP_CMD_ID);
            Map<String, String> model = new HashMap<>();
            model.put(VAR_CMD_META_FIELD, CommandParser.FIELD_CMD_META);
            ClassMeta.Builder cmdExecBuilder = CommandBuilderUtil.getCommandExecutorBuilder(classElement, builderContext);
            cmdExecBuilder.putTransience(FIELD_CMD_META, "_cmdMeta");
            cmdExecBuilder
                    .addImplement(ICommandExecutor.class.getCanonicalName())
                    .addFieldBuilder(FieldMeta.builder()
                        .addModifier(Modifier.PRIVATE)
                        .setName(FIELD_USER_CMD)
                        .setTypeName(cmdBuilder.getQualifiedClassName())
                        .setValue(StringHelper.makeString("new {}();", cmdBuilder.getQualifiedClassName())))
                    .addFieldBuilder(FieldMeta.builder()
                        .addModifier(Modifier.PRIVATE)
                        .setName(FIELD_CMD_META)
                        .setTypeName(metaBuilder.getQualifiedClassName()))
                    .addMethodBuilder(MethodMeta.builder()
                        .addModifier(Modifier.PUBLIC)
                        .setName(cmdExecBuilder.getGeneratedClassName())
                        .setReturnTypeName("")
                        .addParameterBuilder(ParameterMeta.builder()
                            .setName("commandMeta")
                            .setType(metaBuilder.getQualifiedClassName()))
                        .addCodeBuilder(CodeMeta.builder()
                            .addRawCode(StringHelper.makeString("this.{} = commandMeta;", FIELD_CMD_META))))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("commandId")
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempCmdId)));

            // Setup model
            cmdModel.name = cmdName;
            cmdModel.namespace = cmdNs;
            cmdModel.description = cmdDesc;
            cmdModel.parentPath = cmdParentPath;
            cmdModel.userCommandClassName = cmdBuilder.getQualifiedClassName();
            cmdModel.executorClassName = cmdExecBuilder.getQualifiedClassName();
            Map<String, Object> tmpModel = new HashMap<>();
            tmpModel.put("command", cmdModel);

            // Setup template
            Template tmpName = builderContext.loadTemplate(TEMPLATE_NAME);
            Template tmpNamespace = builderContext.loadTemplate(TEMPLATE_NAMESPACE);
            Template tmpParentPath = builderContext.loadTemplate(TEMPLATE_PARENT_PATH);
            Template tmpDesc = builderContext.loadTemplate(TEMPLATE_DESCRIPTION);
            Template tmpNewExec = builderContext.loadTemplate(TEMPLATE_NEW_EXEC);

            // Construct command meta class builder
            metaBuilder
                    .addImplement(ICommandMeta.class.getCanonicalName())
                    .addAnnotationBuilder(AnnotationMeta.builder()
                            .setName(AutoService.class.getCanonicalName())
                            .addArgument(ArgumentMeta.builder()
                                    .setName("value")
                                    .setValue(ICommandMeta.class.getCanonicalName() + ".class")
                                    .setIsString(false)))
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
                                    .setTemplate(tmpParentPath)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName("newExecutor")
                            .setReturnTypeName(ICommandExecutor.class.getCanonicalName())
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tmpModel)
                                    .setTemplate(tmpNewExec)));


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
                .filter(entry -> "parent".equals(entry.getKey().getSimpleName().toString()))
                .map(Map.Entry::getValue)
                .map(annoValue -> (DeclaredType) annoValue.getValue())
                .map(DeclaredType::asElement)
                .first(null);
        if (parentType == null || void.class.getCanonicalName().equals(parentType.getSimpleName().toString())) {
            return ICommandMeta.ROOT_PATH;
        }
        Command parentCommand = parentType.getAnnotation(Command.class);
        if (parentCommand == null) {
            throw new GeneralException(
                    "No Command annotation was declared on class - {}", classElement.getSimpleName().toString());
        }
        if (! namespace.equals(parentCommand.namespace())) {
            throw new GeneralException(
                    "The namespace of parent command does not equals to [{}] - {}", namespace, classElement.getSimpleName().toString());
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
