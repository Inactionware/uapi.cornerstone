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
import uapi.GeneralException;
import uapi.Type;
import uapi.behavior.*;
import uapi.behavior.annotation.Action;
import uapi.behavior.annotation.ActionDo;
import uapi.behavior.annotation.helper.IActionHandlerHelper;
import uapi.codegen.*;
import uapi.common.Numeric;
import uapi.rx.Looper;
import uapi.service.annotation.helper.IServiceHandlerHelper;
import uapi.service.annotation.Service;

import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

/**
 * The handler is used to handle IAction related annotations
 */
@AutoService(IAnnotationsHandler.class)
public class ActionHandler extends AnnotationsHandler {

    private static final String TEMPLATE_GET_ID         = "template/getId_method.ftl";
    private static final String TEMPLATE_INPUT_METAS    = "template/inputMetas_method.ftl";
    private static final String TEMPLATE_OUTPUT_METAS   = "template/outputMetas_method.ftl";
    private static final String TEMPLATE_PROCESS        = "template/process_method.ftl";

    private final ActionHandlerHelper _helper = new ActionHandlerHelper();

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] orderedAnnotations =
            new Class[] { Action.class };

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    @Override
    public IHandlerHelper getHelper() {
        return this._helper;
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
            var action = classElement.getAnnotation(Action.class);
            var actionName = action.value();
            if (Strings.isNullOrEmpty(actionName)) {
                actionName = classElement.asType().toString();
            }

            var actionMeta = this._helper.parseActionMethod(builderContext, classElement);
            var clsBuilder = builderContext.findClassBuilder(classElement);

            var tempGetId = builderContext.loadTemplate(Module.name, TEMPLATE_GET_ID);
            var tempInputMetas = builderContext.loadTemplate(Module.name, TEMPLATE_INPUT_METAS);
            var tempOutputMetas = builderContext.loadTemplate(Module.name, TEMPLATE_OUTPUT_METAS);
            var tempProcess = builderContext.loadTemplate(Module.name, TEMPLATE_PROCESS);
            var model = new HashMap<String, Object>();
            model.put("actionName", actionName);
            model.put("actionMethodName", actionMeta.methodName());
            model.put("actionParameterMetas", actionMeta.parameterMetas());
            model.put("isHandyOutput", actionMeta.isHandyOutput());
            model.put("handyOutputMeta", actionMeta.handyOutputMeta());

            clsBuilder
                    .addImplement(IAction.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("getId")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setReturnTypeName(ActionIdentify.class.getCanonicalName())
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempGetId).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("inputMetas")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setReturnTypeName(Type.toArrayType(ActionInputMeta.class))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempInputMetas).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("outputMetas")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setReturnTypeName(Type.toArrayType(ActionOutputMeta.class))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempOutputMetas).setModel(model)))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("isAnonymous")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setReturnTypeName(Type.BOOLEAN)
                            .addCodeBuilder(CodeMeta.builder().addRawCode("        return true;")))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("process")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .setReturnTypeName(Type.VOID)
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("inputs").setType(Type.toArrayType(Object.class)))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("outputs").setType(Type.toArrayType(ActionOutput.class)))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName("context").setType(IExecutionContext.class.getCanonicalName()))
                            .addCodeBuilder(CodeMeta.builder().setTemplate(tempProcess).setModel(model)));

            // Add IAction as this service's id
            IServiceHandlerHelper svcHelper = builderContext.getHelper(IServiceHandlerHelper.name);
            svcHelper.addServiceId(clsBuilder, IAction.class.getCanonicalName());
        });
    }

    private final class ActionHandlerHelper implements IActionHandlerHelper {

        @Override
        public ActionMethodMeta parseActionMethod(
                final IBuilderContext builderContext,
                final Element classElement
        ) {
            // Check process method
            var actionDoElements = Looper.on(classElement.getEnclosedElements())
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
            var actionDoElement = (ExecutableElement) actionDoElements.get(0);
            var actionMethodName = actionDoElement.getSimpleName().toString();
            var paramElements = actionDoElement.getParameters();
            var paramMetas = new ParameterMeta[paramElements.size()];
            var idxIn = Numeric.mutableInteger();
            var idxOut = Numeric.mutableInteger();
            Looper.on(paramElements).foreachWithIndex((idx, paramElement) -> {
                var className = paramElement.asType().toString();
                ParameterMeta paramMeta;
                if (IExecutionContext.class.getCanonicalName().equals(className)) {
                    // Check context parameter
                    paramMeta = ParameterMeta.newContextMeta();
                } else if (className.indexOf(ActionOutput.class.getCanonicalName()) == 0) {
                    // Check output parameter
                    var genericTypes = builderContext.getGenericTypes(paramElement);
                    if (genericTypes.size() != 1) {
                        throw new GeneralException(
                                "The action output parameter type must define generic type - Action: {}, Method: {}, Parameter: {}",
                                classElement.getSimpleName().toString(), actionMethodName, paramElement.getSimpleName().toString()
                        );
                    }
                    paramMeta = ParameterMeta.newOutputMeta(
                            idxOut.value(), paramElement.getSimpleName().toString(), genericTypes.get(0).toString());
                    idxOut.increase();
                } else {
                    // All other is input parameter
                    paramMeta = ParameterMeta.newInputMeta(idxIn.value(), className);
                    idxIn.increase();
                }

                paramMetas[idx] = paramMeta;
            });
            var isHandyOutput = false;
            ParameterMeta handyOutputMeta = null;
            var returnType = actionDoElement.getReturnType().toString();
            if (idxOut.value() == 0 && ! Type.VOID.equals(returnType)) {
                isHandyOutput = true;
                handyOutputMeta = ParameterMeta.newOutputMeta(returnType);
            }
            return new ActionMethodMeta(actionMethodName, isHandyOutput, handyOutputMeta, paramMetas);
        }
    }
}
