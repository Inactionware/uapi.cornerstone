package uapi.service.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;
import uapi.rx.Looper;
import uapi.service.IServiceLifecycle;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.*;

/**
 * The method with this annotation must be invoked when the service need to be destroyed
 */
public class OnDeactivateParser {

    private static final String METHOD_ON_DESTROY_NAME  = "onDeactivate";

    private static final String TEMP_ON_DESTROY         = "template/onDeactivate_method.ftl";
    private static final String MODEL_ON_DESTROY        = "ModelDestroy";
    private static final String VAR_METHODS             = "methods";

    private final OnDeactivateHelper _helper;

    public OnDeactivateParser() {
        this._helper = new OnDeactivateHelper();
    }

    public void parse(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) {
        builderCtx.getLogger().info("Start processing OnDeactivateParser annotation");
        Looper.on(elements).foreach(element -> {
            if (element.getKind() != ElementKind.METHOD) {
                throw new GeneralException(
                        "The OnDeactivateParser annotation only can be applied on method",
                        element.getSimpleName().toString());
            }
            builderCtx.checkModifiers(element, uapi.service.annotation.OnDeactivate.class, Modifier.PRIVATE, Modifier.STATIC);
            Element classElemt = element.getEnclosingElement();
            builderCtx.checkModifiers(classElemt, uapi.service.annotation.OnDeactivate.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            // Check method
            String methodName = element.getSimpleName().toString();
            ExecutableElement methodElement = (ExecutableElement) element;
            String returnType = methodElement.getReturnType().toString();
            if (! Type.VOID.equals(returnType)) {
                throw new GeneralException(
                        "Expect the method [{}] with OnActivate annotation should return void, but it return - {}",
                        methodName, returnType);
            }
            List paramElements = methodElement.getParameters();
            if (paramElements.size() != 0) {
                throw new GeneralException(
                        "Expect the method [{}] with OnActivate annotation is allowed 0 parameter, but found - {}",
                        methodName, paramElements.size());
            }

            ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
            this._helper.addDestroyMethod(builderCtx, clsBuilder, methodName);
        });
    }

    public void addOnDestroyMethodIfAbsent(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(element -> {
            Element classElemt = element.getEnclosingElement();
            ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
            this._helper.addDestroyMethod(builderCtx, clsBuilder);
        });
    }

    public OnDeactivateHelper getHelper() {
        return this._helper;
    }

    class OnDeactivateHelper {

        public void addDestroyMethod(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String... methodNames
        ) {
            ArgumentChecker.required(builderContext, "builderContext");
            ArgumentChecker.required(classBuilder, "classBuilder");
            ArgumentChecker.required(methodNames, "methodNames");

            Map<String, Object> tempActivateModel = classBuilder.createTransienceIfAbsent(MODEL_ON_DESTROY, HashMap::new);
            Object existingMethods = tempActivateModel.get(VAR_METHODS);
            List<String> methods;
            if (existingMethods == null) {
                methods = new ArrayList<>();
            } else {
                methods = (List<String>) existingMethods;
            }
            Looper.on(methodNames)
                    .filter(methodName -> ! StringHelper.isNullOrEmpty(methodName))
                    .foreach(methods::add);
            tempActivateModel.put(VAR_METHODS, methods);

            List<MethodMeta.Builder> methodBuilders = classBuilder.findMethodBuilders(METHOD_ON_DESTROY_NAME);
            if (methodBuilders.size() > 0) {
                MethodMeta.Builder mbuilder = Looper.on(methodBuilders)
                        .filter(builder -> builder.getReturnTypeName().equals(Type.VOID))
                        .filter(builder -> builder.getParameterCount() == 0)
                        .first();
                if (mbuilder != null) {
                    return;
                }
            }

            Template tempOnActivate = builderContext.loadTemplate(TEMP_ON_DESTROY);
            classBuilder
                    .addImplement(IServiceLifecycle.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .addModifier(Modifier.PUBLIC)
                            .setName(METHOD_ON_DESTROY_NAME)
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addCodeBuilder(CodeMeta.builder()
                                    .setTemplate(tempOnActivate)
                                    .setModel(classBuilder.getTransience(MODEL_ON_DESTROY)))
                            .setReturnTypeName(Type.VOID));
        }
    }
}