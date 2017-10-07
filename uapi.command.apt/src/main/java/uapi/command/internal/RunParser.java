package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.codegen.ParameterMeta;
import uapi.command.ICommandExecutor;
import uapi.command.IMessageOutput;
import uapi.command.annotation.Command;
import uapi.command.annotation.Run;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.*;

public class RunParser {

    private static final String MODEL_COMMAND_EXECUTOR  = "MODEL_COMMAND_EXECUTOR";

    private static final String TEMP_NEW_EXECUTOR       = "template/newExecutor_method.ftl";
    private static final String TEMP_SET_PARAM          = "template/setParameter_method.ftl";
    private static final String TEMP_SET_OPT            = "template/setOption_method.ftl";
    private static final String TEMP_SET_OPT_ARG        = "template/setOption_arg_method.ftl";
    private static final String TEMP_SET_OUTPU          = "template/setOutput_method.ftl";

    private static final String VAR_RUN_METHOD_NAME     = "runMethodName";
    private static final String VAR_PARAMS              = "parameters";
    private static final String VAR_OPTS                = "options";
    private static final String VAR_OUTPUT              = "outputFieldName";

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(methodElement -> {
            if (methodElement.getKind() != ElementKind.METHOD) {
                throw new GeneralException(
                        "The element {} must be a method element", methodElement.getSimpleName().toString());
            }
            Element classElement = methodElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(methodElement, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            String runMethodName = methodElement.getSimpleName().toString();

            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            Map<String, Object> model = classBuilder.getTransience(MODEL_COMMAND_EXECUTOR);
            if (model != null) {
                throw new GeneralException(
                        "Only one Run annotation is allowed declare in a class - {}",
                        classElement.getSimpleName().toString());
            }
            model = new HashMap<>();
            List<ParamModel> params = classBuilder.getTransience(ParameterParser.MODEL_CMD_PARAM);
            if (params == null) {
                params = new ArrayList<>();
            }
            List<OptionModel> options = classBuilder.getTransience(OptionParser.MODEL_COMMAND_OPTIONS);
            if (options == null) {
                options = new ArrayList<>();
            }
            String outputField = classBuilder.getTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME);
            model.put(VAR_RUN_METHOD_NAME, runMethodName);
            model.put(VAR_PARAMS, params);
            model.put(VAR_OPTS, options);
            model.put(VAR_OUTPUT, outputField);

            classBuilder.putTransience(MODEL_COMMAND_EXECUTOR, model);

            Template tempNewExecutor = builderContext.loadTemplate(TEMP_NEW_EXECUTOR);
            Template tempSetParam = builderContext.loadTemplate(TEMP_SET_PARAM);
            Template tempSetOpt = builderContext.loadTemplate(TEMP_SET_OPT);
            Template tempSetOptArg = builderContext.loadTemplate(TEMP_SET_OPT_ARG);
            Template tempSetOutput = builderContext.loadTemplate(TEMP_SET_OUTPU);

            // Generate newExecutor method
            classBuilder.addImplement(ICommandExecutor.class.getCanonicalName())
                    .addFieldBuilder(FieldMeta.builder()
                            .addModifier(Modifier.PRIVATE)
                            .setName("_msgOut")
                            .setTypeName(IMessageOutput.class.getCanonicalName()))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOutput")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("output")
                                    .setType(IMessageOutput.class.getCanonicalName()))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .addRawCode(StringHelper.makeString("this.{} = output;", "_msgOut"))))
                    .addMethodBuilder(MethodMeta.builder()
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
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("setOutput")
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("output")
                                    .setType(IMessageOutput.class.getCanonicalName()))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempSetOutput)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setName("newExecutor")
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(model)
                                    .setTemplate(tempNewExecutor)));
        });
    }

//    private String generateExecutorClass(IBuilderContext builderContext, Element classElement) {
//        String pkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
//        String execClassName = classElement.getSimpleName().toString() + "_Executor_Generated";
//        ClassMeta.Builder execBuilder = builderContext.newClassBuilder(pkgName, execClassName);
//
//        // TODO: Generate all method
//
//        return execBuilder.getQulifiedClassName();
//    }
}
