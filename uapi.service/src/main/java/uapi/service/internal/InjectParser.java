/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import com.google.common.base.Strings;
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
    private static final String INJECT_METHODS              = "InjectMethods";

    private final InjectParserHelper _helper = new InjectParserHelper();

    InjectParserHelper getHelper() {
        return this._helper;
    }

    public void parse(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) throws GeneralException {
        elements.forEach(annotatedElement -> {
            ElementKind elementKind = annotatedElement.getKind();
            if (elementKind != ElementKind.FIELD && elementKind != ElementKind.METHOD) {
                throw new GeneralException(
                        "The Inject annotation only can be applied on field",
                        annotatedElement.getSimpleName().toString());
            }
            builderCtx.checkModifiers(annotatedElement, Inject.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            Element classElemt = annotatedElement.getEnclosingElement();
            builderCtx.checkModifiers(classElemt, Inject.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            Inject inject = annotatedElement.getAnnotation(Inject.class);
            String injectId = inject.value();
            String injectFrom = inject.from();
            if (Strings.isNullOrEmpty(injectFrom)) {
                throw new GeneralException(
                        "The inject service from [{}.{}] must be specified",
                        classElemt.getSimpleName().toString(),
                        annotatedElement.getSimpleName().toString());
            }

            if (elementKind == ElementKind.FIELD) {
                String fieldName = annotatedElement.getSimpleName().toString();
                String fieldTypeName = annotatedElement.asType().toString();
                boolean isCollection = isCollection(annotatedElement, builderCtx);
                boolean isMap = isMap(annotatedElement, builderCtx);
                String setterName = ClassHelper.makeSetterName(fieldName, isCollection, isMap);
                String idType = null;
                if (isCollection) {
                    List<TypeMirror> typeArgs = getTypeArguments(annotatedElement);
                    if (typeArgs.size() != 1) {
                        throw new GeneralException(
                                "The collection field [{}.{}] must be define only ONE type argument",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    fieldTypeName = typeArgs.get(0).toString();
                } else if (isMap) {
                    List<TypeMirror> typeArgs = getTypeArguments(annotatedElement);
                    if (typeArgs.size() != 2) {
                        throw new GeneralException(
                                "The map field [{}.{}] must be define only TWO type arguments",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    idType = typeArgs.get(0).toString();
                    Types typeUtils = builderCtx.getTypeUtils();
                    Elements elemtUtils = builderCtx.getElementUtils();
                    TypeElement identifiableElemt = elemtUtils.getTypeElement(IIdentifiable.class.getCanonicalName());
                    DeclaredType identifiableType = typeUtils.getDeclaredType(identifiableElemt);
                    if (!typeUtils.isAssignable(typeArgs.get(1), identifiableType)) {
                        throw new GeneralException(
                                "The value type of the field [{}.{}] must be implement IIdentifiable interface",
                                classElemt.getSimpleName().toString(),
                                annotatedElement.getSimpleName().toString());
                    }
                    fieldTypeName = typeArgs.get(1).toString();
                }

                if (Strings.isNullOrEmpty(injectId)) {
                    injectId = fieldTypeName;
                }

                ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
                addSetter(clsBuilder, fieldName, fieldTypeName, injectId, injectFrom, setterName, isCollection, isMap, idType, false);
            } else if (elementKind == ElementKind.METHOD) {
                String methodName = annotatedElement.getSimpleName().toString();
                ExecutableElement methodElemt = (ExecutableElement) annotatedElement;
                String returnType = methodElemt.getReturnType().toString();
                if (! Type.VOID.equals(returnType)) {
                    throw new GeneralException(
                            "Expect then injected method [{}] return void, but it return - {}",
                            methodName, returnType
                    );
                }
                List paramElements = methodElemt.getParameters();
                if (paramElements.size() != 1) {
                    throw new GeneralException(
                            "Expect the injected method [{}] has only 1 parameter, but found - {}",
                            methodName, paramElements.size()
                    );
                }
                VariableElement paramElem = (VariableElement) paramElements.get(0);
                String paramType = paramElem.asType().toString();
                // Remove generic type
                if (paramType.contains("<")) {
                    paramType = paramType.substring(0, paramType.indexOf("<"));
                }

                if (Strings.isNullOrEmpty(injectId)) {
                    injectId = paramType;
                }

                ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
                List injectMethods = clsBuilder.getTransience(INJECT_METHODS);
                if (injectMethods == null) {
                    injectMethods = new ArrayList();
                    clsBuilder.putTransience(INJECT_METHODS, injectMethods);
                }
                injectMethods.add(new InjectMethod(methodName, injectId, paramType, injectFrom));
            }
        });

        Template tempInject = builderCtx.loadTemplate(TEMPLATE_INJECT);
        Template tempGetDependencies = builderCtx.loadTemplate(TEMPLATE_GET_DEPENDENCIES);
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
            FieldMeta.Builder fieldMeta = classBuilder.findFieldBuilder(fieldName, fieldType);
            if (fieldMeta == null) {
                classBuilder.addFieldBuilder(FieldMeta.builder()
                        .addModifier(Modifier.PRIVATE)
                        .setName(fieldName)
                        .setTypeName(fieldType)
                        .setIsList(isCollection)
                        .setIsMap(isMap));
            }
        }

        String paramName = SETTER_PARAM_NAME;
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
        Elements elemtUtils = builderCtx.getElementUtils();
        Types typeUtils = builderCtx.getTypeUtils();
        WildcardType wildcardType = typeUtils.getWildcardType(null, null);
        TypeElement collectionTypeElemt = elemtUtils.getTypeElement(
                Collection.class.getCanonicalName());
        DeclaredType collectionType = typeUtils.getDeclaredType(
                collectionTypeElemt, wildcardType);
        return typeUtils.isAssignable(fieldElement.asType(), collectionType);
    }

    private boolean isMap(
            final Element fieldElement,
            final IBuilderContext builderCtx) {
        Elements elemtUtils = builderCtx.getElementUtils();
        Types typeUtils = builderCtx.getTypeUtils();
        WildcardType wildcardType = typeUtils.getWildcardType(null, null);
        TypeElement collectionTypeElemt = elemtUtils.getTypeElement(
                Map.class.getCanonicalName());
        DeclaredType mapType = typeUtils.getDeclaredType(
                collectionTypeElemt, wildcardType, wildcardType);
        return typeUtils.isAssignable(fieldElement.asType(), mapType);
    }

    private List<TypeMirror> getTypeArguments(Element fieldElement) {
        final List<TypeMirror> typeArgs = new ArrayList<>();
        DeclaredType declaredType = (DeclaredType) fieldElement.asType();
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
//        Template tempDependencies = builderCtx.loadTemplate(TEMPLATE_GET_DEPENDENCIES);
        Map<String, Object> tempModelDependencies = new HashMap<>();
        tempModelDependencies.put("dependencies", dependencies);
        if (classBuilder.findSetterBuilders().size() == 0) {
            // No setters means this class does not implement IInjectable interface
            return;
        }
        classBuilder.overrideMethodBuilder(MethodMeta.builder()
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
        String methodName = "injectObject";
        String paramName = "injection";
        String paramType = Injection.class.getName();

        final List<SetterModel> setterModels = new ArrayList<>();
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
        Map<String, Object> tempModel = new HashMap<>();
        tempModel.put("setters", setterModels);

        if (classBuilder.findSetterBuilders().size() == 0) {
            // No setters means this class does not implement IInjectable interface
            return;
        }
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
            String setterName = ClassHelper.makeSetterName(fieldName, isCollection, isMap);
            InjectParser.this.addSetter(classBuilder, fieldName, fieldType, injectId, injectFrom, setterName, isCollection, isMap, mapKeyType, true);
            Template tempInjectObject = builderContext.loadTemplate(TEMPLATE_INJECT);
            Template tempGetDependencies = builderContext.loadTemplate(TEMPLATE_GET_DEPENDENCIES);
            implementInjectObjectForClass(classBuilder, tempInjectObject);
            implementGetDependenciesForClass(classBuilder, tempGetDependencies);
        }
    }

    private static final class InjectMethod {

        private String _methodName;
        private String _injectId;
        private String _injectType;
        private String _injectFrom;

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

        private String methodName() {
            return this._methodName;
        }

        private String injectId() {
            return this._injectId;
        }

        private String injectType() {
            return this._injectType;
        }

        private String injectFrom() {
            return this._injectFrom;
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
