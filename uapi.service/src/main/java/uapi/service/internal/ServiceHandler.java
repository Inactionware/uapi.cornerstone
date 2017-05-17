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
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IService;
import uapi.service.IServiceFactory;
import uapi.service.IServiceHandlerHelper;
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

    private static final String MODEL_GET_IDS               = "ModelGetId";
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
            Service service = classElement.getAnnotation(Service.class);
            boolean autoActive = service.autoActive();
            String[] serviceIds = mergeId(getTypesInAnnotation(svcAnnoMirror, "value"), service.ids());
            if (serviceIds.length == 0) {
                final StringBuilder svcId = new StringBuilder();
                // Check service factory type argument first
//                Observable.from(((TypeElement) classElement).getInterfaces())
//                        .filter(declareType -> declareType.toString().startsWith(IServiceFactory.class.getName()))
//                        .map(declareType -> ((DeclaredType) declareType).getTypeArguments().get(0))
//                        .subscribe(svcId::append);
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
            Template tempGetIds = builderCtx.loadTemplate(TEMPLATE_GET_IDS);
            this._helper.addServiceId(classBuilder, serviceIds);

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
//            builderCtx.getLogger().info("End handle annotation {} for class {}",
//                    annotationType, classElement.getSimpleName().toString());
        });
    }

    private String[] mergeId(List<String> serviceTypes, String[] serviceIds) {
        List<String> ids = new ArrayList<>();
//        Observable.from(serviceTypes).subscribe(ids::add);
//        Observable.from(serviceIds).subscribe(ids::add);
        Looper.on(serviceTypes).foreach(ids::add);
        Looper.on(serviceIds).foreach(ids::add);
        return ids.toArray(new String[ids.size()]);
    }

//    /**
//     * Get service type from AnnotationMirror
//     * Since the class type can't be received directly at annotation processing time
//     * so we have to get from AnnotationMirror
//     *
//     * @param   serviceAnnotation
//     *          The AnnotationMirror which is related with Service annotation
//     * @return  The type name array
//     */
//    @SuppressWarnings("unchecked")
//    private List<String> getTypes(AnnotationMirror serviceAnnotation) {
//        List<String> types = new ArrayList<>();
//        Observable.from(serviceAnnotation.getElementValues().values())
//                .flatMap(annoValue -> Observable.from((List<AnnotationValue>) annoValue.getValue()))
//                .map(annoValue -> (DeclaredType) annoValue.getValue())
//                .map(declaredType -> (TypeElement) declaredType.asElement())
//                .map(typeElem -> typeElem.getQualifiedName().toString())
//                .subscribe(types::add);
//        return types;
//    }

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
//                Observable.from(idArr).subscribe(idList::add);
//                Observable.from(serviceIds).filter(id -> ! idList.contains(id)).subscribe(idList::add);
                Looper.on(idArr).foreach(idList::add);
                Looper.on(serviceIds).filter(id -> ! idList.contains(id)).foreach(idList::add);
                idArr = idList.toArray(new String[idList.size()]);
            }
            tempGetIdsModel.put(VAR_SVC_IDS, idArr);
        }
    }
}
