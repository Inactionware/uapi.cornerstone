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
import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.service.annotation.Service;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A annotation handler used to handler Service annotation
 */
@AutoService(IAnnotationsHandler.class)
public final class ServiceHandler extends AnnotationsHandler {

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] orderedAnnotations = new Class[] { Service.class };

    private static final String TEMPLATE_GET_IDS            = "template/getIds_method.ftl";
    private static final String TEMPLATE_REQ_ATTRS          = "template/requiredAttributes_method.ftl";
    private static final String TEMPLATE_INST_CONSTRUCTOR   = "template/instance_constructor.ftl";
    private static final String TEMPLATE_ATTRS              = "template/attributes_method.ftl";

    private static final String MODEL_GET_IDS               = "ModelGetId";
    private static final String MODEL_REQ_ATTRS             = "ModelRequiredAttributes";
    private static final String VAR_SVC_IDS                 = "serviceIds";

    private final ServiceHandlerHelper _helper = new ServiceHandlerHelper();

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
            final IBuilderContext builderCtx,
            final Class<? extends Annotation> annotationType,
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
            ClassMeta.Builder classBuilder = builderCtx.findClassBuilder(classElement);
            AnnotationMirror svcAnnoMirror = MoreElements.getAnnotationMirror(classElement, Service.class).get();
            String pkgName = builderCtx.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
            Service service = classElement.getAnnotation(Service.class);
            boolean autoActive = service.autoActive();
            ServiceType svcType = service.type();
            String[] serviceIds = mergeId(getTypesInAnnotation(svcAnnoMirror, "value"), service.ids());
            if (serviceIds.length == 0) {
                final StringBuilder svcId = new StringBuilder();
                // Check service factory type argument first
                Looper.on(((TypeElement) classElement).getInterfaces())
                        .filter(declareType -> declareType.toString().startsWith(IServiceFactory.class.getName()))
                        .map(declareType -> ((DeclaredType) declareType).getTypeArguments().get(0))
                        .foreach(svcId::append);
                if (svcId.length() == 0) {
                    // If the service is not a factory, using service class type
                    svcId.append(StringHelper.makeString("{}.{}",
                            classBuilder.getPackageName(), classElement.getSimpleName().toString()));
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
                constructPrototypeService(builderCtx, classBuilder, serviceIds[0], pkgName, classElement.getSimpleName().toString());
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
            final String packageName,
            final String prototypeId,
            final String instClassName
    ) {
        Template tempReqAttrs = builderContext.loadTemplate(TEMPLATE_REQ_ATTRS);
        Template tempInstCons = builderContext.loadTemplate(TEMPLATE_INST_CONSTRUCTOR);
        Template tempAttrs = builderContext.loadTemplate(TEMPLATE_ATTRS);
        Map<String, Object> modelReqAttrs = instClassBuilder.createTransienceIfAbsent(MODEL_REQ_ATTRS, HashMap::new);

        // instance service
        instClassBuilder
                .addImplement(IInstance.class.getCanonicalName())
                .addFieldBuilder(FieldMeta.builder()
                        .setName("_attributes")
                        .addModifier(Modifier.PRIVATE, Modifier.FINAL)
                        .setIsMap(true)
                        .setKeyTypeName(Type.STRING)
                        .setTypeName("?"))
                // Constructor
                .addMethodBuilder(MethodMeta.builder()
                        .setName(instClassName)
                        .addModifier(Modifier.PUBLIC)
                        .addParameterBuilder(ParameterMeta.builder()
                                .setName("attributes")
                                .setType("java.util.Map<String, ?>"))
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempInstCons)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName(IInstance.METHOD_ATTRIBUTES)
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
        ClassMeta.Builder prototypeBuilder = builderContext.newClassBuilder(packageName, instClassName + "_Prototype_Generated");
        prototypeBuilder
                .addAnnotationBuilder(AnnotationMeta.builder()
                        .setName(AutoService.class.getCanonicalName())
                        .addArgument(ArgumentMeta.builder()
                                .setName("value")
                                .setIsString(false)
                                .setValue(IService.class.getCanonicalName() + ".class")))
                .addImplement(IPrototype.class.getCanonicalName())
                .addMethodBuilder(MethodMeta.builder()
                        .setName("attributes")
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .setReturnTypeName(Type.Q_STRING_ARRAY)
                        .addCodeBuilder(CodeMeta.builder()
                                .setTemplate(tempAttrs)
                                .setModel(modelReqAttrs)))
                .addMethodBuilder(MethodMeta.builder()
                        .setName("newInstance")
                        .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                        .setReturnTypeName(IInstance.class.getCanonicalName())
                        .addParameterBuilder(ParameterMeta.builder()
                                .setName("attributes")
                                .setType("java.util.Map<String, ?>"))
                        .addCodeBuilder(CodeMeta.builder()
                                .addRawCode("return new {}(attributes);", instClassName)));

    }

    private void constructService(
            final IBuilderContext builderCtx,
            final ClassMeta.Builder classBuilder,
            final boolean autoActive
    ) {
        Template tempGetIds = builderCtx.loadTemplate(TEMPLATE_GET_IDS);
//        this._helper.addServiceId(classBuilder, serviceIds);

        // Build class builder
        classBuilder
                .addAnnotationBuilder(AnnotationMeta.builder()
                        .setName(AutoService.class.getCanonicalName())
                        .addArgument(ArgumentMeta.builder()
                                .setName("value")
                                .setIsString(false)
                                .setValue(IService.class.getCanonicalName() + ".class")))
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

            Map<String, Object> tempGetIdsModel = classBuilder.createTransienceIfAbsent(MODEL_GET_IDS, HashMap::new);
            String[] idArr = (String[]) tempGetIdsModel.get(VAR_SVC_IDS);
            if (idArr == null) {
                idArr = serviceIds;
            } else {
                List<String> idList = new ArrayList<>();
                Looper.on(idArr).foreach(idList::add);
                Looper.on(serviceIds).filter(id -> ! idList.contains(id)).foreach(idList::add);
                idArr = idList.toArray(new String[idList.size()]);
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
