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
import uapi.GeneralException;
import uapi.Type;
import uapi.behavior.annotation.Action;
import uapi.codegen.*;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * The handler is used to handle IAction related annotations
 */
@AutoService(IAnnotationsHandler.class)
public class ActionHandler extends AnnotationsHandler {

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

            TypeElement typeElement = (TypeElement) classElement;
            List<TypeMirror> intfTypes = (List<TypeMirror>) typeElement.getInterfaces();
            DeclaredType actionType = Looper.on(intfTypes)
                    .filter(intfType -> intfType instanceof DeclaredType)
                    .map(intfType -> (DeclaredType) intfType)
                    .first();
            if (actionType == null) {
                throw new GeneralException(
                        "The action class {} must implement IAction interface and specified its parameterized types",
                        classElement.getSimpleName().toString());
            }
            List typeArgs = actionType.getTypeArguments();
            if (typeArgs.size() != 2) {
                throw new GeneralException(
                        "The parameterized types of IAction must be 2 - {}", classElement.getSimpleName().toString());
            }

            Action action = classElement.getAnnotation(Action.class);
            String actionName = action.value();
            String inputType = typeArgs.get(0).toString(); //(DeclaredType) typeArgs.get(0)).asElement().getSimpleName().toString();
            String outputType = typeArgs.get(1).toString(); //((DeclaredType) typeArgs.get(1)).asElement().getSimpleName().toString();

            ClassMeta.Builder clsBuilder = builderContext.findClassBuilder(classElement);
            clsBuilder
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("name")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(Type.STRING)
                            .addCodeBuilder(CodeMeta.builder().addRawCode(StringHelper.makeString("return \"{}\";", actionName))))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("inputType")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(StringHelper.makeString("java.lang.Class<{}>", inputType))
                            .addCodeBuilder(CodeMeta.builder().addRawCode(StringHelper.makeString("return {}.class;", inputType))))
                    .addMethodBuilder(MethodMeta.builder()
                            .setName("outputType")
                            .addModifier(Modifier.PUBLIC)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .setReturnTypeName(StringHelper.makeString("java.lang.Class<{}>", outputType))
                            .addCodeBuilder(CodeMeta.builder().addRawCode(StringHelper.makeString("return {}.class;", outputType))));
        });
    }
}
