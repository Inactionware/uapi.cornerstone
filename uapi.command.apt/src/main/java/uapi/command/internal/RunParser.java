package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.codegen.ParameterMeta;
import uapi.command.CommandResult;
import uapi.command.ICommandExecutor;
import uapi.command.IMessageOutput;
import uapi.command.annotation.Command;
import uapi.command.annotation.Run;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.*;

public class RunParser {

    private static final String MODEL_COMMAND_EXECUTOR  = "MODEL_COMMAND_EXECUTOR";

    private static final String TEMP_SET_PARAM          = "template/setParameter_method.ftl";
    private static final String TEMP_SET_OPT            = "template/setOption_method.ftl";
    private static final String TEMP_SET_OPT_ARG        = "template/setOption_arg_method.ftl";
    private static final String TEMP_SET_OUTPU          = "template/setOutput_method.ftl";
    private static final String TEMP_EXECUTE            = "template/execute_method.ftl";

    private static final String VAR_RUN_METHOD_NAME     = "runMethodName";
    private static final String VAR_PARAMS              = "parameters";
    private static final String VAR_OPTS                = "options";
    private static final String VAR_OUTPUT              = "outputFieldName";
    private static final String VAR_USER_CMD_FIELD      = "userCommandField";

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(methodElement -> {
            if (methodElement.getKind() != ElementKind.METHOD) {
                throw new GeneralException(
                        "The element {} must be a method element", methodElement.getSimpleName().toString());
            }
            var classElement = methodElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(methodElement, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            var runMethodName = methodElement.getSimpleName().toString();
            // The run method must be no argument and return boolean to figure out success or failure
            var execElement = (ExecutableElement) methodElement;
            var paramElements = execElement.getParameters();
            if (paramElements.size() != 0) {
                throw new GeneralException(
                        "The method annotated with Run must be has no input parameter - {}", runMethodName);
            }
            var returnType = execElement.getReturnType().toString();
            if (! Type.BOOLEAN.equals(returnType) && ! Type.Q_BOOLEAN.equals(returnType)) {
                throw new GeneralException(
                        "The method annotated with Run must return a boolean type - {}", runMethodName);
            }

            var cmdMetaBuilder = CommandBuilderUtil.getCommandMetaBuilder(classElement, builderContext);
            CommandModel cmdModel = cmdMetaBuilder.getTransience(CommandHandler.CMD_MODEL);

            var cmdExecBuilder = CommandBuilderUtil.getCommandExecutorBuilder(classElement, builderContext);
            Map<String, Object> model = cmdExecBuilder.getTransience(MODEL_COMMAND_EXECUTOR);
            if (model != null) {
                throw new GeneralException(
                        "Only one Run annotation is allowed declare in a class - {}",
                        classElement.getSimpleName().toString());
            }
            model = new HashMap<>();
            var outputField = cmdExecBuilder.getTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME);
            model.put(VAR_RUN_METHOD_NAME, runMethodName);
            model.put(VAR_PARAMS, cmdModel.parameters);
            model.put(VAR_OPTS, cmdModel.options);
            model.put(VAR_OUTPUT, outputField);
            model.put(VAR_USER_CMD_FIELD, CommandParser.FIELD_USER_CMD);

            cmdExecBuilder.putTransience(MODEL_COMMAND_EXECUTOR, model);

            var tempSetParam = builderContext.loadTemplate(Module.name, TEMP_SET_PARAM);
            var tempSetOpt = builderContext.loadTemplate(Module.name, TEMP_SET_OPT);
            var tempSetOptArg = builderContext.loadTemplate(Module.name, TEMP_SET_OPT_ARG);
            var tempSetOutput = builderContext.loadTemplate(Module.name, TEMP_SET_OUTPU);
            var tempExec = builderContext.loadTemplate(Module.name, TEMP_EXECUTE);

            // Generate newExecutor method
            cmdExecBuilder.addImplement(ICommandExecutor.class.getCanonicalName())
                    .addFieldBuilder(FieldMeta.builder()
                            .addModifier(Modifier.PRIVATE)
                            .setName("_msgOut")
                            .setTypeName(IMessageOutput.class.getCanonicalName()))
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOutput")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("output")
                                    .setType(IMessageOutput.class.getCanonicalName()))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .addRawCode(StringHelper.makeString("this.{} = output;", "_msgOut"))))
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setParameter")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("name")
                                    .setType(Type.Q_STRING))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("value")
                                    .setType(Type.Q_OBJECT))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempSetParam)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOption")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("name")
                                    .setType(Type.Q_STRING))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempSetOpt)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOption")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("name")
                                    .setType(Type.Q_STRING))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("argument")
                                    .setType(Type.Q_STRING))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempSetOptArg)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOutput")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("output")
                                    .setType(IMessageOutput.class.getCanonicalName()))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempSetOutput)))
                    .addMethodBuilder((MethodMeta.builder())
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("execute")
                            .setReturnTypeName(CommandResult.class.getCanonicalName())
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempExec)));
        });
    }
}
