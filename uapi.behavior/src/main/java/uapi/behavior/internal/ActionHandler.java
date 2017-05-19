/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.behavior.ActionIdentify;
import uapi.behavior.IAction;
import uapi.behavior.IExecutionContext;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.codegen.*;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IServiceHandlerHelper;
import uapi.service.annotation.Service;

import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The handler is used to handle IAction related annotations
 */
@AutoService(IAnnotationsHandler.class)
public class ActionHandler extends AnnotationsHandler {

    private static final String TEMPLATE_GET_ID         = "template/getId_method.ftl";
    private static final String TEMPLATE_INPUT_TYPE     = "template/inputType_method.ftl";
    private static final String TEMPLATE_OUTPUT_TYPE    = "template/outputType_method.ftl";
    private static final String TEMPLATE_PROCESS        = "template/process_method.ftl";

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] orderedAnnotations =
            new Class[] { Action.class };

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        if (annotationType != Action.class) {
            throw new GeneralException("Unsupported annotation type - {}", annotationType.getCanonicalName());
        }

        Looper.on(elements).foreach(classElement -> {
            if (classElement.getKind() != ElementKind.CLASS) {
                throw new GeneralException(
                        "The element {} must be a class element", classElement.getSimpleName().toString());
            }
            builderContext.checkAnnotations(classElement, Service.class);
            Action action = classElement.getAnnotation(Action.class);
            String actionName = action.value();
            if (Strings.isNullOrEmpty(actionName)) {
                actionName = classElement.asType().toString();
            }
            // Check process method
            List actionDoElements = Looper.on(classElement.getEnclosedElements())
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .filter(element -> element.getAnnotation(ActionDo.class) != null)
                    .toList();
            if (actionDoElements.size() == 0) {
                throw new GeneralException(
                        "The action class {} must define a method which is annotated with ActionDo annotation",
                        classElement.getSimpleName().toString());
            }
            if (actionDoElements.size() > 1) {
                throw new GeneralException(
                        "The action class {} define more methods which is annotated with ActionDo annotation",
                        classElement.getSimpleName().toString());
            }
            ExecutableElement actionDoElement = (ExecutableElement) actionDoElements.get(0);
            String actionMethodName = actionDoElement.getSimpleName().toString();
            List paramElements = actionDoElement.getParameters();
            String inputType;
            String outputType;
            boolean needContext = false;
            if (paramElements.size() == 0) {
                throw new GeneralException(
                        "The method annotated with ActionDo must contains 1 or 2 parameters - {}::{}",
                        classElement.getSimpleName().toString(), actionMethodName);
            } else if (paramElements.size() > 2) {
                throw new GeneralException(
                        "The method annotated with ActionDo must contains more than 2 parameters - {}::{}",
                        classElement.getSimpleName().toString(), actionMethodName);
            } else {
                VariableElement inputParamElement = (VariableElement) paramElements.get(0);
                inputType = inputParamElement.asType().toString();
                if (paramElements.size() == 1) {
                    if (IExecutionContext.class.getCanonicalName().equals(inputType)) {
                        inputType = Type.VOID;
                        needContext = true;
                    }
                } else if (paramElements.size() == 2) {
                    VariableElement contextParamElement = (VariableElement) paramElements.get(1);
                    if (! IExecutionContext.class.getCanonicalName().equals(contextParamElement.asType().toString())) {
                        throw new GeneralException(
                                "The second parameter of method which annotated with ActionDo must be IExecutionContext - {}::{}",
                                classElement.getSimpleName().toString(), actionMethodName);
                    }
                    needContext = true;
                }
            }
            outputType = actionDoElement.getReturnType().toString();
            // convert native type to associated qualified type
            inputType = Type.toQType(inputType);
            outputType = Type.toQType(outputType);

            Template tempGetId = builderContext.loadTemplate(TEMPLATE_GET_ID);
            Template tempInputType = builderContext.loadTemplate(TEMPLATE_INPUT_TYPE);
            Template tempOutputType = builderContext.loadTemplate(TEMPLATE_OUTPUT_TYPE);
            Template tempProcess = builderContext.loadTemplate(TEMPLATE_PROCESS);
            Map<String, Object> model = new HashMap<>();
            model.put("actionName", actionName);
            model.put("actionMethodName", actionMethodName);
            model.put("inputType", inputType);
            model.put("outputType", outputType);
            model.put("needContext", needContext);
            model.put("isInVoid", Type.Q_VOID.equals(inputType));
            model.put("isOutVoid", Type.Q_VOID.equals(outputType));

            ClassMeta.Builder clsBuilder = builderContext.findClassBuilder(classElement);
            clsBuilder
                    .addImplement(StringHelper.makeString(
                            "{}<{}, {}>", IAction.class.getCanonicalName(), inputType, outputType))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("getId")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(ActionIdentify.class.getCanonicalName())
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempGetId).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("inputType")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(StringHelper.makeString("java.lang.Class<{}>", inputType))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempInputType).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("outputType")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(StringHelper.makeString("java.lang.Class<{}>", outputType))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempOutputType).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("process")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(outputType)
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("input").setType(inputType))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("context").setType(IExecutionContext.class.getCanonicalName()))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempProcess).setModel(model)));

            // Add IAction as this service's id
            IServiceHandlerHelper svcHelper = (IServiceHandlerHelper) builderContext.getHelper(IServiceHandlerHelper.name);
            svcHelper.addServiceId(clsBuilder, IAction.class.getCanonicalName());
        });
    }
}
