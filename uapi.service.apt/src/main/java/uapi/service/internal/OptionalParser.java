/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.InvalidArgumentException;
import uapi.Type;
import uapi.codegen.*;
import uapi.rx.Looper;
import uapi.service.IServiceLifecycle;
import uapi.service.SetterMeta;

import javax.lang.model.element.*;
import java.util.*;

/**
 * The handler is used to handle Optional annotation
 */
class OptionalParser {

    private static final String TEMPLATE_IS_OPTIONAL    = "template/isOptional_method.ftl";

    private static final String MODEL_IS_OPTIONAL       = "optionals";

    private final OptionalParserHelper _helper = new OptionalParserHelper();

    OptionalParserHelper getHelper() {
        return this._helper;
    }

    public void parse(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) throws GeneralException {
        builderCtx.getLogger().info("Starting process Option annotation");
        // Initialize optional setters
        elements.forEach(element -> {
            builderCtx.checkModifiers(element, uapi.service.annotation.Optional.class,
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            builderCtx.checkAnnotations(element, uapi.service.annotation.Inject.class);
            var classElemt = element.getEnclosingElement();
            builderCtx.checkModifiers(classElemt, uapi.service.annotation.Optional.class,
                    Modifier.PRIVATE, Modifier.FINAL);

            // Ensure the class must implements IServiceLifecycle interface
//            if (! builderCtx.isAssignable(classElemt, IServiceLifecycle.class)) {
//                throw new GeneralException(
//                        "The service dependency ({}) is optional, the service must implements IServiceLifecycle interface - {}",
//                        element.getSimpleName().toString(), classElemt.getSimpleName().toString());
//            }

            if (element.getKind() == ElementKind.FIELD) {
                var fieldName = element.getSimpleName().toString();

                var clsBuilder = builderCtx.findClassBuilder(classElemt);
                setOptionalDependency(clsBuilder, fieldName);
            } else if (element.getKind() == ElementKind.METHOD) {
                var methodName = element.getSimpleName().toString();
                var clsBuilder = builderCtx.findClassBuilder(classElemt);
                List<InjectParser.InjectMethod> injectMethods = clsBuilder.getTransience(InjectParser.INJECT_METHODS);
                List<InjectParser.InjectMethod> matchedMethods = Looper.on(injectMethods)
                        .filter(method -> methodName.equals(method.methodName()))
                        .toList();
                InjectParser.InjectMethod matchedMethod = null;
                if (matchedMethods.size() == 1) {
                    matchedMethod = matchedMethods.get(0);
                } else {
                    var methodElemt = (ExecutableElement) element;
                    var paramElements = methodElemt.getParameters();
                    if (paramElements.size() != 1) {
                        throw new GeneralException(
                                "Expect the injected method [{}] has only 1 parameter, but found - {}",
                                methodName, paramElements.size()
                        );
                    }
                    var paramElem = (VariableElement) paramElements.get(0);
                    var rawParamType = paramElem.asType().toString();
                    // Remove generic type
                    var paramType = rawParamType.contains("<") ?
                            rawParamType.substring(0, rawParamType.indexOf("<")) : rawParamType;
                    matchedMethods = Looper.on(matchedMethods)
                            .filter(method -> paramType.equals(method.injectType()))
                            .toList();
                    if (matchedMethods.size() == 1) {
                        matchedMethod = matchedMethods.get(0);
                    }
                }
                if (matchedMethod == null) {
                    throw new GeneralException(
                            "Can't found inject information for method which annotated with Optional - {}", methodName);
                }
                matchedMethod.setOptional(true);
            } else {
                throw new GeneralException(
                        "The Optional annotation only can be applied on field or method",
                        element.getSimpleName().toString());
            }
        });


        builderCtx.getBuilders().forEach(classBuilder -> {
            final var temp = builderCtx.loadTemplate(Module.name, TEMPLATE_IS_OPTIONAL);
            // builderCtx.getLogger().info("Generate isOptional for {}", classBuilder.getClassName());
            implementOptional(classBuilder, temp);
        });
    }

    private void implementOptional(ClassMeta.Builder classBuilder, Template temp) {
        String methodName       = "isOptional";
        String methodReturnType = Type.BOOLEAN;
        String paramName        = "id";
        String paramType        = Type.STRING;

        final List<String> optionals = new ArrayList<>();
        var setters = classBuilder.findSetterBuilders();
        Looper.on(setters)
                .map(setter -> (SetterMeta.Builder) setter)
                .filter(SetterMeta.Builder::getIsOptional)
                .foreach(setter -> optionals.add(setter.getInjectId()));

        List<InjectParser.InjectMethod> injectMethods = classBuilder.getTransience(InjectParser.INJECT_METHODS);
        if (injectMethods != null) {
            Looper.on(injectMethods)
                    .filter(InjectParser.InjectMethod::isOptional)
                    .foreach(injectMethod -> optionals.add(injectMethod.injectId()));
        }

        if (setters.size() == 0 && (injectMethods == null || injectMethods.size() == 0)) {
            // This class does not implement IInjectable interface
            return;
        }

        final var tempModel = new HashMap<String, List<String>>();
        tempModel.put(MODEL_IS_OPTIONAL, optionals);

        classBuilder.addMethodBuilder(MethodMeta.builder()
                .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                .setName(methodName)
                .setReturnTypeName(methodReturnType)
                .addModifier(Modifier.PUBLIC)
                .addThrowTypeName(InvalidArgumentException.class.getName())
                .addParameterBuilder(ParameterMeta.builder()
                        .setName(paramName)
                        .setType(paramType))
                .addCodeBuilder(CodeMeta.builder()
                        .setTemplate(temp)
                        .setModel(tempModel)));
    }

    private void setOptionalDependency(ClassMeta.Builder classBuilder, String fieldName) {
        classBuilder.findSetterBuilders().stream()
                .map(setter -> (SetterMeta.Builder) setter)
                .filter(setter -> setter.getFieldName().equals(fieldName))
                .forEach(setter -> setter.setIsOptional(true));
    }

    class OptionalParserHelper {

        void setOptional(IBuilderContext builderContext, ClassMeta.Builder classBuilder, String fieldName) {
            setOptionalDependency(classBuilder, fieldName);
            final Template temp = builderContext.loadTemplate(Module.name, TEMPLATE_IS_OPTIONAL);
            implementOptional(classBuilder, temp);
        }
    }
}
