package uapi.service.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IServiceLifecycle;
import uapi.service.annotation.OnInject;

import javax.lang.model.element.*;
import java.util.*;

/**
 * The parser is used to parse OnInject annotation
 */
public class OnInjectParser {

    private static final String METHOD_NAME     = "onDependencyInject";
    private static final String PARAM_SVC_ID    = "serviceId";
    private static final String PARAM_SVC       = "service";

    private static final String TEMP_ON_INJECT  = "template/onInject_method.ftl";
    private static final String MODEL_ON_INJECT = "Model";
    private static final String VAR_METHODS     = "methods";
    private static final String VAR_METHOD_NAME = "methodName";
    private static final String VAR_SVC_ID      = "serviceId";
    private static final String VAR_SVC_TYPE    = "serviceType";

    private final OnInjectHelper _helper;

    public OnInjectParser() {
        this._helper = new OnInjectHelper();
    }

    public OnInjectHelper getHelper() {
        return this._helper;
    }

    public void parse(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) {
        builderCtx.getLogger().info("Start processing OnInject annotation");
        Looper.on(elements).foreach(element -> {
            if (element.getKind() != ElementKind.METHOD) {
                throw new GeneralException(
                        "The OnInject annotation only can be applied on method",
                        element.getSimpleName().toString());
            }
            builderCtx.checkModifiers(element, OnInject.class, Modifier.PRIVATE, Modifier.STATIC);
            var classElemt = element.getEnclosingElement();
            builderCtx.checkModifiers(classElemt, OnInject.class, Modifier.PRIVATE, Modifier.FINAL);

            // Check method
            var methodName = element.getSimpleName().toString();
            var methodElement = (ExecutableElement) element;
            var returnType = methodElement.getReturnType().toString();
            if (! Type.VOID.equals(returnType)) {
                throw new GeneralException(
                        "Expect the method [{}] with OnInject annotation should return void, but it return - {}",
                        methodName, returnType);
            }
            var paramElements = methodElement.getParameters();
            if (paramElements.size() != 1) {
                throw new GeneralException(
                        "Expect the method [{}] with OnInject annotation is allowed 1 parameter only, but found - {}",
                        methodName, paramElements.size());
            }
            // get injected service type
            var paramElem = (VariableElement) paramElements.get(0);
            var serviceType = paramElem.asType().toString();
            // Remove generic type
            if (serviceType.contains("<")) {
                serviceType = serviceType.substring(0, serviceType.indexOf("<"));
            }

            // get inject service id
            var onInject = element.getAnnotation(OnInject.class);
            var serviceId = onInject.value();
            if (StringHelper.isNullOrEmpty(serviceId)) {
                serviceId = serviceType;
            }

            var clsBuilder = builderCtx.findClassBuilder(classElemt);
            this._helper.addInjectMethod(builderCtx, clsBuilder, methodName, serviceId, serviceType);
        });
    }

    public void addInjectMethodIfAbsent(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(element -> {
            Element classElemt = element.getEnclosingElement();
            ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
            this._helper.addInjectMethod(builderCtx, clsBuilder, null, null, null);
        });
    }

    public class OnInjectHelper {

        public void addInjectMethod(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String methodName,
                final String serviceId,
                final String serviceType) {
            ArgumentChecker.required(builderContext, "builderContext");
            ArgumentChecker.required(classBuilder, "classBuilder");

            var tempInjectModel = classBuilder.createTransienceIfAbsent(MODEL_ON_INJECT, HashMap::new);
            var existingMethods = tempInjectModel.get(VAR_METHODS);
            List<Map<String, String>> methods;
            if (existingMethods == null) {
                methods = new ArrayList<>();
                tempInjectModel.put(VAR_METHODS, methods);
            } else {
                methods = (List<Map<String, String>>) existingMethods;
            }
            if (methodName != null) {
                Map<String, String> method = new HashMap<>();
                method.put(VAR_METHOD_NAME, methodName);
                method.put(VAR_SVC_ID, serviceId);
                method.put(VAR_SVC_TYPE, serviceType);
                methods.add(method);
            }

            var methodBuilders = classBuilder.findMethodBuilders(METHOD_NAME);
            if (methodBuilders.size() > 0) {
                var mbuilder = Looper.on(methodBuilders)
                        .filter(builder -> builder.getReturnTypeName().equals(Type.VOID))
                        .filter(builder -> builder.getParameterCount() == 2)
                        .filter(builder -> {
                            var paramBuilder = builder.findParameterBuilder(PARAM_SVC_ID);
                            return paramBuilder != null && paramBuilder.getType().equals(Type.Q_STRING);
                        })
                        .filter(builder -> {
                            var paramBuilder = builder.findParameterBuilder(PARAM_SVC);
                            return paramBuilder != null && paramBuilder.getType().equals(Type.Q_OBJECT);
                        })
                        .first();
                if (mbuilder != null) {
                    return;
                }
            }

            var tempOnInject = builderContext.loadTemplate(TEMP_ON_INJECT);
            classBuilder
                    .addImplement(IServiceLifecycle.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .setName(METHOD_NAME)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName("Override"))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName(PARAM_SVC_ID)
                                    .setType(Type.Q_STRING))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName(PARAM_SVC)
                                    .setType(Type.Q_OBJECT))
                            .addCodeBuilder(CodeMeta.builder()
                                    .setTemplate(tempOnInject)
                                    .setModel(classBuilder.getTransience(MODEL_ON_INJECT)))
                            .setReturnTypeName(Type.VOID));
        }
    }
}
