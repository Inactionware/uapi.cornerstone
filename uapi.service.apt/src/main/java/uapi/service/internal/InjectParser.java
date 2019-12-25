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
import uapi.IIdentifiable;
import uapi.InvalidArgumentException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.service.annotation.Inject;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The handler for Inject annotation
 */
class InjectParser {

    private static final String TEMPLATE_INJECT             = "template/inject_method.ftl";
    private static final String TEMPLATE_GET_DEPENDENCIES   = "template/getDependencies_method.ftl";
    private static final String SETTER_PARAM_NAME           = "value";
    public static final String INJECT_METHODS               = "InjectMethods";

    private final InjectParserHelper _helper = new InjectParserHelper();

    InjectParserHelper getHelper() {
        return this._helper;
    }

    public void parse(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) throws GeneralException {
        elements.forEach(annotatedElement -> {
            var elementKind = annotatedElement.getKind();
            if (elementKind != ElementKind.FIELD && elementKind != ElementKind.METHOD) {
                throw new GeneralException(
                        "The Inject annotation only can be applied on field or method",
                        annotatedElement.getSimpleName().toString());
            }
            builderCtx.checkModifiers(annotatedElement, Inject.class, Modifier.PRIVATE, Modifier.STATIC);
            var classElemt = annotatedElement.getEnclosingElement();
            builderCtx.checkModifiers(classElemt, Inject.class, Modifier.PRIVATE, Modifier.FINAL);

            var inject = annotatedElement.getAnnotation(Inject.class);
            var injectId = inject.value();
            var injectFrom = inject.from();
            if (StringHelper.isNullOrEmpty(injectFrom)) {
                throw new GeneralException(
                        "The inject service from [{}.{}] must be specified",
                        classElemt.getSimpleName().toString(),
                        annotatedElement.getSimpleName().toString());
            }

            if (elementKind == ElementKind.FIELD) {
                var fieldName = annotatedElement.getSimpleName().toString();
                var fieldTypeName = annotatedElement.asType().toString();
                var isCollection = isCollection(annotatedElement, builderCtx);
                var isMap = isMap(annotatedElement, builderCtx);
                var setterName = ClassHelper.makeSetterName(fieldName, isCollection, isMap);
                String idType = null;
                if (isCollection) {
                    var typeArgs = getTypeArguments(annotatedElement);
                    if (typeArgs.size() != 1) {
                        throw new GeneralException(
                                "The collection field [{}.{}] must be define only ONE type argument",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    fieldTypeName = typeArgs.get(0).toString();
                } else if (isMap) {
                    var typeArgs = getTypeArguments(annotatedElement);
                    if (typeArgs.size() != 2) {
                        throw new GeneralException(
                                "The map field [{}.{}] must be define only TWO type arguments",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    idType = typeArgs.get(0).toString();
                    var typeUtils = builderCtx.getTypeUtils();
                    var elemtUtils = builderCtx.getElementUtils();
                    var identifiableElemt = elemtUtils.getTypeElement(IIdentifiable.class.getCanonicalName());
                    var identifiableType = typeUtils.getDeclaredType(identifiableElemt);
                    if (!typeUtils.isAssignable(typeArgs.get(1), identifiableType)) {
                        throw new GeneralException(
                                "The value type of the field [{}.{}] must be implement IIdentifiable interface",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    fieldTypeName = typeArgs.get(1).toString();
                } else {
                    // All injectable field can't be modified by final except collection or map field
                    builderCtx.checkModifiers(annotatedElement, Inject.class, Modifier.FINAL);
                }

                if (StringHelper.isNullOrEmpty(injectId)) {
                    injectId = fieldTypeName;
                }

                var clsBuilder = builderCtx.findClassBuilder(classElemt);
                addSetter(clsBuilder, fieldName, fieldTypeName, injectId, injectFrom, setterName, isCollection, isMap, idType, false);
            } else if (elementKind == ElementKind.METHOD) {
                var methodName = annotatedElement.getSimpleName().toString();
                var methodElemt = (ExecutableElement) annotatedElement;
                var returnType = methodElemt.getReturnType().toString();
                if (! Type.VOID.equals(returnType)) {
                    throw new GeneralException(
                            "Expect the injected method [{}] return void, but it return - {}",
                            methodName, returnType
                    );
                }
                var paramElements = methodElemt.getParameters();
                if (paramElements.size() != 1) {
                    throw new GeneralException(
                            "Expect the injected method [{}] is only allowed 1 parameter, but found - {} parameters",
                            methodName, paramElements.size()
                    );
                }
                var paramElem = (VariableElement) paramElements.get(0);
                var paramType = paramElem.asType().toString();
                // Remove generic type
                if (paramType.contains("<")) {
                    paramType = paramType.substring(0, paramType.indexOf("<"));
                }

                if (StringHelper.isNullOrEmpty(injectId)) {
                    injectId = paramType;
                }

                ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
                List<InjectMethod> injectMethods = clsBuilder.getTransience(INJECT_METHODS);
                if (injectMethods == null) {
                    injectMethods = new ArrayList<>();
                    clsBuilder.putTransience(INJECT_METHODS, injectMethods);
                }
                injectMethods.add(new InjectMethod(methodName, injectId, paramType, injectFrom));
            }
        });

        var tempInject = builderCtx.loadTemplate(Module.name, TEMPLATE_INJECT);
        var tempGetDependencies = builderCtx.loadTemplate(Module.name, TEMPLATE_GET_DEPENDENCIES);
        builderCtx.getBuilders().forEach(classBuilder -> {
            implementInjectObjectForClass(classBuilder, tempInject);
            implementGetDependenciesForClass(classBuilder, tempGetDependencies);
        });
    }

    private void addSetter(
            final ClassMeta.Builder classBuilder,
            final String fieldName,
            final String fieldType,
            final String injectId,
            final String injectFrom,
            final String setterName,
            final boolean isCollection,
            final boolean isMap,
            final String mapKeyType,
            final boolean needGenerateField) {
        SetterMeta.Builder setterBdr = Looper.on(classBuilder.findSetterBuilders())
                .filter(methodBuilder -> methodBuilder.getName().equals(setterName))
                .map(methodBuilder -> (SetterMeta.Builder) methodBuilder)
                .filter(setterBuilder -> setterBuilder.getInjectType().equals(fieldType))
                .first(null);
        if (setterBdr != null) {
            return;
        }

        if (needGenerateField) {
            // Generate field
            var fieldMeta = classBuilder.findFieldBuilder(fieldName, fieldType);
            if (fieldMeta == null) {
                classBuilder.addFieldBuilder(FieldMeta.builder()
                        .addModifier(Modifier.PRIVATE)
                        .setName(fieldName)
                        .setTypeName(fieldType)
                        .setIsList(isCollection)
                        .setIsMap(isMap));
            }
        }

        var paramName = SETTER_PARAM_NAME;
        String code;
        if (isCollection) {
            code = StringHelper.makeString("{}.add({});", fieldName, paramName);
        } else if (isMap) {
            code = StringHelper.makeString("{}.put( ({}) (({}) {}).getId(), {} );",
                    fieldName, mapKeyType, IIdentifiable.class.getCanonicalName(), paramName, paramName);
        } else {
            code = StringHelper.makeString("{}={};", fieldName, paramName);
        }
        classBuilder
                .addImplement(IInjectable.class.getCanonicalName())
                .addMethodBuilder(SetterMeta.builder()
                        .setIsSingle(! isCollection && ! isMap)
                        .setFieldName(fieldName)
                        .setInjectId(injectId)
                        .setInjectFrom(injectFrom)
                        .setInjectType(fieldType)
                        .setName(setterName)
                        .setReturnTypeName(Type.VOID)
                        .setInvokeSuper(MethodMeta.InvokeSuper.NONE)
                        .addParameterBuilder(ParameterMeta.builder()
                                .addModifier(Modifier.FINAL)
                                .setName(paramName)
                                .setType(fieldType))
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode(code)));
    }

    private boolean isCollection(
            final Element fieldElement,
            final IBuilderContext builderCtx) {
        var elemtUtils = builderCtx.getElementUtils();
        var typeUtils = builderCtx.getTypeUtils();
        var wildcardType = typeUtils.getWildcardType(null, null);
        var collectionTypeElemt = elemtUtils.getTypeElement(
                Collection.class.getCanonicalName());
        var collectionType = typeUtils.getDeclaredType(
                collectionTypeElemt, wildcardType);
        return typeUtils.isAssignable(fieldElement.asType(), collectionType);
    }

    private boolean isMap(
            final Element fieldElement,
            final IBuilderContext builderCtx) {
        var elemtUtils = builderCtx.getElementUtils();
        var typeUtils = builderCtx.getTypeUtils();
        var wildcardType = typeUtils.getWildcardType(null, null);
        var collectionTypeElemt = elemtUtils.getTypeElement(
                Map.class.getCanonicalName());
        var mapType = typeUtils.getDeclaredType(
                collectionTypeElemt, wildcardType, wildcardType);
        return typeUtils.isAssignable(fieldElement.asType(), mapType);
    }

    private List<TypeMirror> getTypeArguments(Element fieldElement) {
        final List<TypeMirror> typeArgs = new ArrayList<>();
        var declaredType = (DeclaredType) fieldElement.asType();
        declaredType.getTypeArguments().forEach(
                typeMirror -> typeArgs.add(typeMirror));
        return typeArgs;
    }

    private void implementGetDependenciesForClass(ClassMeta.Builder classBuilder, Template temp) {
        // Receive service dependency id list
        List<MethodMeta.Builder> setterBuilders = classBuilder.findSetterBuilders();
        List<DependencyModel> dependencies = Looper.on(setterBuilders)
                .map(builder -> (SetterMeta.Builder) builder)
                .map(setterBuilder -> {
                    DependencyModel depModel = new DependencyModel(
                            QualifiedServiceId.combine(setterBuilder.getInjectId(), setterBuilder.getInjectFrom()),
                            setterBuilder.getInjectId(),
                            setterBuilder.getInjectType());
                    depModel.setSingle(setterBuilder.getIsSingle());
                    return depModel;
                })
                .toList();
        List<InjectMethod> injectMethods = classBuilder.getTransience(INJECT_METHODS);
        if (injectMethods != null && injectMethods.size() > 0) {
            Looper.on(injectMethods).foreach(injectMethod -> {
                dependencies.add(new DependencyModel(
                        QualifiedServiceId.combine(injectMethod.injectId(), injectMethod.injectFrom()),
                        injectMethod.injectId(),
                        injectMethod.injectType()));
            });
        }
        // Check duplicated dependency
        dependencies.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.summingInt(p -> 1)))
                .forEach((dependSvc, counter) -> {
                    if (counter > 1) {
                        throw new GeneralException(StringHelper.makeString(
                                "The service {}.{} has duplicated dependency on same service {}",
                                classBuilder.getPackageName(),
                                classBuilder.getClassName(),
                                dependSvc));
                    }
                });
        if (dependencies.size() == 0) {
            return;
        }

        var tempModelDependencies = new HashMap<String, Object>();
        tempModelDependencies.put("dependencies", dependencies);
        classBuilder
                .addImplement(IInjectable.class.getCanonicalName())
                .overrideMethodBuilder(MethodMeta.builder()
                .addAnnotationBuilder(AnnotationMeta.builder()
                        .setName(AnnotationMeta.OVERRIDE))
                .setName("getDependencies")
                .addModifier(Modifier.PUBLIC)
                .setReturnTypeName(StringHelper.makeString("{}[]", Dependency.class.getName()))
                .addCodeBuilder(CodeMeta.builder()
                        .setTemplate(temp)
                        .setModel(tempModelDependencies)));
    }

    private void implementInjectObjectForClass(ClassMeta.Builder classBuilder, Template temp) {
        var methodName = "injectObject";
        var paramName = "injection";
        var paramType = Injection.class.getName();

        final var setterModels = new ArrayList<SetterModel>();
        classBuilder.findSetterBuilders().forEach(methodBuilder -> {
            SetterMeta.Builder setterBuilder = (SetterMeta.Builder) methodBuilder;
            setterModels.add(new SetterModel(
                    setterBuilder.getName(),
                    setterBuilder.getInjectId(),
                    setterBuilder.getInjectType()));
        });
        List<InjectMethod> injectMethods = classBuilder.getTransience(INJECT_METHODS);
        if (injectMethods != null && injectMethods.size() > 0) {
            Looper.on(injectMethods).foreach(injectMethod -> {
                setterModels.add(new SetterModel(
                        injectMethod.methodName(),
                        injectMethod.injectId(),
                        injectMethod.injectType()
                ));
            });
        }
        if (setterModels.size() == 0) {
            return;
        }

        var tempModel = new HashMap<String, Object>();
        tempModel.put("setters", setterModels);
        classBuilder
                .addImplement(IInjectable.class.getCanonicalName())
                .overrideMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder()
                                .setName("Override"))
                        .addModifier(Modifier.PUBLIC)
                        .setName(methodName)
                        .setReturnTypeName(Type.VOID)
                        .addThrowTypeName(InvalidArgumentException.class.getCanonicalName())
                        .addParameterBuilder(ParameterMeta.builder()
                                .addModifier(Modifier.FINAL)
                                .setName(paramName)
                                .setType(paramType))
                        .addCodeBuilder(CodeMeta.builder()
                                .setModel(tempModel)
                                .setTemplate(temp)));
    }

    class InjectParserHelper {

        public void addDependency(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String fieldName,
                final String fieldType,
                final String injectId,
                final String injectFrom,
                final boolean isCollection,
                final boolean isMap,
                final String mapKeyType) {
            var setterName = ClassHelper.makeSetterName(fieldName, isCollection, isMap);
            InjectParser.this.addSetter(classBuilder, fieldName, fieldType, injectId, injectFrom, setterName, isCollection, isMap, mapKeyType, true);
            var tempInjectObject = builderContext.loadTemplate(Module.name, TEMPLATE_INJECT);
            var tempGetDependencies = builderContext.loadTemplate(Module.name, TEMPLATE_GET_DEPENDENCIES);
            implementInjectObjectForClass(classBuilder, tempInjectObject);
            implementGetDependenciesForClass(classBuilder, tempGetDependencies);
        }
    }

    public static class InjectMethod {

        private String _methodName;
        private String _injectId;
        private String _injectType;
        private String _injectFrom;
        private boolean _optional = false;

        private InjectMethod(
                final String methodName,
                final String injectId,
                final String injectType,
                final String injectFrom
        ) {
            this._methodName = methodName;
            this._injectId = injectId;
            this._injectType = injectType;
            this._injectFrom = injectFrom;
        }

        public String methodName() {
            return this._methodName;
        }

        public String injectId() {
            return this._injectId;
        }

        public String injectType() {
            return this._injectType;
        }

        public String injectFrom() {
            return this._injectFrom;
        }

        public void setOptional(boolean optional) {
            this._optional = optional;
        }

        public boolean isOptional() {
            return this._optional;
        }
    }

    public static final class SetterModel {

        private String _name;
        private String _injectId;
        private String _injectType;

        private SetterModel(
                final String name,
                final String injectId,
                final String injectType
        ) throws InvalidArgumentException {
            ArgumentChecker.notEmpty(name, "name");
            ArgumentChecker.notEmpty(injectId, "injectId");
            ArgumentChecker.notEmpty(injectType, "injectType");
            this._name = name;
            this._injectId = injectId;
            this._injectType = injectType;
        }

        public String getName() {
            return this._name;
        }

        public String getInjectId() {
            return this._injectId;
        }

        public String getInjectType() {
            return this._injectType;
        }
    }

    public static final class DependencyModel {

        private String _qSvcId;
        private String _svcId;
        private String _svcType;
        private boolean _single;

        private DependencyModel(
                final String qualifiedServiceId,
                final String serviceId,
                final String serviceType
        ) {
            this._qSvcId = qualifiedServiceId;
            this._svcId = serviceId;
            this._svcType = serviceType;
        }

        public String getQualifiedServiceId() {
            return this._qSvcId;
        }

        public String getServiceType() {
            return this._svcType;
        }

        public String getServiceId() {
            return this._svcId;
        }

        public void setServiceId(String serviceId) {
            this._svcId = serviceId;
        }

        public void setSingle(boolean single) {
            this._single = single;
        }

        public boolean getSingle() {
            return this._single;
        }
    }
}
