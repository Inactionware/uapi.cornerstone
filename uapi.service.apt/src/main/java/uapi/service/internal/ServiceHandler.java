/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.service.annotation.Attribute;
import uapi.service.annotation.Service;
import uapi.service.annotation.helper.IServiceHandlerHelper;
import uapi.service.annotation.helper.ServiceType;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.tools.StandardLocation;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A annotation handler used to handler Service annotation
 */
@AutoService(IAnnotationsHandler.class)
public final class ServiceHandler extends AnnotationsHandler {

    private static final String GEN_PKG_NAME    = "uapi.generated";

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] orderedAnnotations = new Class[] {
            Service.class, Attribute.class
    };

    private static final String TEMPLATE_GET_IDS            = "template/getIds_method.ftl";
    private static final String TEMPLATE_REQ_ATTRS          = "template/requiredAttributes_method.ftl";
    private static final String TEMPLATE_INST_CONSTRUCTOR   = "template/instance_constructor.ftl";
    private static final String TEMPLATE_ATTRS              = "template/attributes_method.ftl";

    private static final String MODEL_GET_IDS               = "ModelGetId";
    private static final String MODEL_REQ_ATTRS             = "ModelRequiredAttributes";
    private static final String VAR_SVC_IDS                 = "serviceIds";
    private static final String VAR_ATTRS                   = "attrs";

    // Below variable is store to class build to indicate prototype service information
    static final String VAR_IS_PROTOTYPE                    = "isPrototype";
    static final String VAR_PROTOTYPE_CLASS_NAME            = "prototypeClassName";

    private final ServiceHandlerHelper _helper = new ServiceHandlerHelper();

    private IResourceFile _svcResFile;

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    @Override
    public IHandlerHelper getHelper() {
        return this._helper;
    }

    @Override
    public void init(IBuilderContext builderContext) throws GeneralException {
        this._svcResFile = builderContext.newResourceFile(
                StandardLocation.CLASS_OUTPUT, IServiceModulePortal.SERVICE_FILE_NAME);

    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderCtx,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        if (annotationType.equals(Service.class)) {
            handleServiceAnnotation(builderCtx, elements);
        } else if (annotationType.equals(Attribute.class)) {
            handleAttributeAnnotation(builderCtx, elements);
        } else {
            throw new GeneralException("Unsupported annotation - {}", annotationType.getClass().getName());
        }
    }

    private void handleAttributeAnnotation(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) throws GeneralException {
        Looper.on(elements).foreach(fieldElement -> {
            if (fieldElement.getKind() != ElementKind.FIELD) {
                throw new GeneralException(
                        "The Attribute annotation only can be applied on field - {}",
                        fieldElement.getSimpleName().toString());
            }
            builderCtx.checkModifiers(fieldElement, Attribute.class, Modifier.PRIVATE, Modifier.FINAL);

            var classElement = fieldElement.getEnclosingElement();
            builderCtx.checkModifiers(classElement, Service.class, Modifier.PRIVATE, Modifier.FINAL);
            var annoSvc = classElement.getAnnotation(Service.class);
            if (annoSvc.type() != ServiceType.Prototype) {
                throw new GeneralException(
                        "Attribute annotation is only support declare in prototype service - {}",
                        classElement.getSimpleName().toString());
            }

            var annoAttr = fieldElement.getAnnotation(Attribute.class);
            var attrName = annoAttr.value();
            var attrField = fieldElement.getSimpleName().toString();
            var attrFieldType = Type.toQType(fieldElement.asType().toString());
            var instClassBuilder = builderCtx.findClassBuilder(classElement);
            var modelReqAttrs = instClassBuilder.createTransienceIfAbsent(MODEL_REQ_ATTRS, HashMap::new);
            var requiredAttrs = (List<AttributeMode>) modelReqAttrs.get(VAR_ATTRS);
            if (requiredAttrs == null) {
                requiredAttrs = new ArrayList<>();
                modelReqAttrs.put(VAR_ATTRS, requiredAttrs);
            }
            var attrInfo = new AttributeMode(attrName, attrField, attrFieldType);
            requiredAttrs.add(attrInfo);
        });
    }

    private void handleServiceAnnotation(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) throws GeneralException {
        elements.forEach(classElement -> {
            if (classElement.getKind() != ElementKind.CLASS) {
                throw new GeneralException(
                        "The Service annotation only can be applied on class - {}",
                        classElement.getSimpleName().toString());
            }
            builderCtx.checkModifiers(classElement, Service.class, Modifier.PRIVATE, Modifier.FINAL);

//            builderCtx.getLogger().info("Start handle annotation {} for class {}",
//                    annotationType, classElement.getSimpleName().toString());
            // Receive service id array
            var classBuilder = builderCtx.findClassBuilder(classElement);
            var svcAnnoMirror = MoreElements.getAnnotationMirror(classElement, Service.class).get();
//            var pkgName = builderCtx.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
            var service = classElement.getAnnotation(Service.class);
            var autoActive = service.autoActive();
            var svcType = service.type();
            var serviceIds = mergeId(getTypesInAnnotation(svcAnnoMirror, "value"), service.ids());
            if (serviceIds.length == 0) {
                final var svcId = new StringBuilder();
                // Check service factory type argument first
                Looper.on(((TypeElement) classElement).getInterfaces())
                        .filter(declareType -> declareType.toString().startsWith(IServiceFactory.class.getName()))
                        .map(declareType -> ((DeclaredType) declareType).getTypeArguments().get(0))
                        .foreach(svcId::append);
                if (svcId.length() == 0) {
                    // If the service is not a factory, using service class type
                    svcId.append(classBuilder.getParentClassName());
                }
                serviceIds = new String[] { svcId.toString() };
            }
            if (svcType == ServiceType.Prototype && serviceIds.length > 1) {
                throw new GeneralException(
                        "Prototype service is only allow one service id - {}, ids - {}",
                        classElement.getSimpleName().toString(), serviceIds);
            }
            this._helper.addServiceId(classBuilder, serviceIds);

            // Build class builder
            if (svcType == ServiceType.Prototype) {
                constructPrototypeService(builderCtx, classBuilder, serviceIds[0], classElement.getSimpleName().toString());
            } else {
                constructService(builderCtx, classBuilder, autoActive);
            }
        });
    }

    private String[] mergeId(List<String> serviceTypes, String[] serviceIds) {
        List<String> ids = new ArrayList<>();
        Looper.on(serviceTypes).foreach(ids::add);
        Looper.on(serviceIds).foreach(ids::add);
        return ids.toArray(new String[ids.size()]);
    }

    private void constructPrototypeService(
            final IBuilderContext builderContext,
            final ClassMeta.Builder instClassBuilder,
//            final String packageName,
            final String prototypeId,
            final String userClassName
    ) {
        var tempReqAttrs = builderContext.loadTemplate(Module.name, TEMPLATE_REQ_ATTRS);
        var tempInstCons = builderContext.loadTemplate(Module.name, TEMPLATE_INST_CONSTRUCTOR);
        var tempAttrs = builderContext.loadTemplate(Module.name, TEMPLATE_ATTRS);
        var modelReqAttrs = instClassBuilder.createTransienceIfAbsent(MODEL_REQ_ATTRS, HashMap::new);
        if (! modelReqAttrs.containsKey(VAR_ATTRS)) {
            modelReqAttrs.put(VAR_ATTRS, new ArrayList<>());
        }

        // instance service
        instClassBuilder
                .addImplement(IInstance.class.getCanonicalName())
                .addFieldBuilder(FieldMeta.builder()
                        .setName("_attributes")
                        .addModifier(Modifier.PRIVATE)
                        .setIsMap(true)
                        .setKeyTypeName(Type.STRING)
                        .setTypeName("?"))
                // Constructor
                .addMethodBuilder(MethodMeta.builder()
                        .setName(instClassBuilder.getGeneratedClassName())
                        .addModifier(Modifier.PUBLIC)
                        .addParameterBuilder(ParameterMeta.builder()
                                .setName("attributes")
                                .setType("java.util.Map<String, ?>"))
                        .addCodeBuilder(CodeMeta.builder()
                                .setModel(modelReqAttrs)
                                .setTemplate(tempInstCons)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName(IInstance.METHOD_ATTRIBUTES)
                        .setReturnTypeName("java.util.Map<String, ?>")
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .addModifier(Modifier.PUBLIC)
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode("return this._attributes;")))
                .addMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .setName(IInstance.METHOD_PROTOTYPE_ID)
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(Type.Q_STRING)
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode("return \"{}\";", prototypeId)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName("requiredAttributes")
                        .setReturnTypeName(Type.Q_STRING_ARRAY)
                        .addModifier(Modifier.PRIVATE)
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempReqAttrs)
                                .setModel(modelReqAttrs)));

        // Prototype service
        var tempGetIds = builderContext.loadTemplate(Module.name, TEMPLATE_GET_IDS);
        var tempGetIdsModel = new HashMap<>();
        tempGetIdsModel.put(VAR_SVC_IDS, new String[] { prototypeId });
        var idArr = (String[]) tempGetIdsModel.get(VAR_SVC_IDS);
        var prototypeBuilder = builderContext.newClassBuilder(
                instClassBuilder.getPackageName(), userClassName + "_Prototype_Generated");
        prototypeBuilder
//                .addAnnotationBuilder(AnnotationMeta.builder()
//                        .setName(AutoService.class.getCanonicalName())
//                        .addArgument(ArgumentMeta.builder()
//                                .setName("value")
//                                .setIsString(false)
//                                .setValue(IService.class.getCanonicalName() + ".class")))
                .addImplement(IPrototype.class.getCanonicalName())
                .addMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder()
                                .setName(AnnotationMeta.OVERRIDE))
                        .setName(IService.METHOD_AUTOACTIVE)
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(IService.METHOD_AUTOACTIVE_RETURN_TYPE)
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode(StringHelper.makeString("return {};", false))))
                .addMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder()
                                .setName(AnnotationMeta.OVERRIDE))
                        .setName(IService.METHOD_GETIDS)
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(IService.METHOD_GETIDS_RETURN_TYPE)
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempGetIds)
                                .setModel(tempGetIdsModel)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName("attributes")
                        .addModifier(Modifier.PUBLIC)
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .setReturnTypeName(Type.Q_STRING_ARRAY)
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempAttrs)
                                .setModel(modelReqAttrs)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName("newInstance")
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(IInstance.class.getCanonicalName())
                        .addParameterBuilder(ParameterMeta.builder()
                                .setName("attributes")
                                .setType("java.util.Map<String, ?>"))
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode("return new {}(attributes);", instClassBuilder.getGeneratedClassName())));

        instClassBuilder.putTransience(VAR_IS_PROTOTYPE, true);
        instClassBuilder.putTransience(VAR_PROTOTYPE_CLASS_NAME, prototypeBuilder.getQualifiedClassName());

        // Add service to service file
        this._svcResFile.appendContent(prototypeBuilder.getQualifiedClassName() + "\n");
    }

    private void constructService(
            final IBuilderContext builderCtx,
            final ClassMeta.Builder classBuilder,
            final boolean autoActive
    ) {
        var tempGetIds = builderCtx.loadTemplate(Module.name, TEMPLATE_GET_IDS);
//        this._helper.addServiceId(classBuilder, serviceIds);

        // Build class builder
        classBuilder
//                .addAnnotationBuilder(AnnotationMeta.builder()
//                        .setName(AutoService.class.getCanonicalName())
//                        .addArgument(ArgumentMeta.builder()
//                                .setName("value")
//                                .setIsString(false)
//                                .setValue(IService.class.getCanonicalName() + ".class")))
                .addImplement(IService.class.getCanonicalName())
                .addMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder()
                                .setName(AnnotationMeta.OVERRIDE))
                        .setName(IService.METHOD_GETIDS)
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(IService.METHOD_GETIDS_RETURN_TYPE)
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempGetIds)
                                .setModel(classBuilder.getTransience(MODEL_GET_IDS))))
                .addMethodBuilder(MethodMeta.builder()
                        .addAnnotationBuilder(AnnotationMeta.builder()
                                .setName(AnnotationMeta.OVERRIDE))
                        .setName(IService.METHOD_AUTOACTIVE)
                        .addModifier(Modifier.PUBLIC)
                        .setReturnTypeName(IService.METHOD_AUTOACTIVE_RETURN_TYPE)
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode(StringHelper.makeString("return {};", autoActive))));

        // Add service to service file
        this._svcResFile.appendContent(classBuilder.getQualifiedClassName() + "\n");
    }

    public static final class AttributeMode {

        private String _name;
        private String _field;
        private String _type;

        private AttributeMode(
                final String name,
                final String field,
                final String type
        ) {
            this._name = name;
            this._field = field;
            this._type = type;
        }

        public String getName() {
            return this._name;
        }

        public String getField() {
            return this._field;
        }

        public String getType() {
            return this._type;
        }

        public String toString() {
            return StringHelper.makeString("name={}, field={}, type={}", getName(), this._field, this._type);
        }
    }

    private final class ServiceHandlerHelper implements IServiceHandlerHelper {

        private ServiceHandlerHelper() { }

        @Override
        public String getName() {
            return IServiceHandlerHelper.name;
        }

        @Override
        public void addServiceId(ClassMeta.Builder classBuilder, String... serviceIds) {
            ArgumentChecker.required(serviceIds, "serviceIds");

            var tempGetIdsModel = classBuilder.createTransienceIfAbsent(MODEL_GET_IDS, HashMap::new);
            var idArr = (String[]) tempGetIdsModel.get(VAR_SVC_IDS);
            if (idArr == null) {
                idArr = serviceIds;
            } else {
                List<String> idList = new ArrayList<>();
                Looper.on(idArr).foreach(idList::add);
                Looper.on(serviceIds).filter(id -> ! idList.contains(id)).foreach(idList::add);
                idArr = idList.toArray(new String[0]);
            }
            tempGetIdsModel.put(VAR_SVC_IDS, idArr);
        }

        @Override
        public void becomeService(IBuilderContext builderCtx, ClassMeta.Builder classBuilder, String... serviceIds) {
            addServiceId(classBuilder, serviceIds);
            constructService(builderCtx, classBuilder, false);
        }
    }
}
