package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.annotation.Command;
import uapi.command.annotation.Run;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunParser {

    private static final String MODEL_COMMAND_RUN   = "MODEL_COMMAND_RUN";
    private static final String TEMP_NEW_EXECUTOR   = "newExecutor";

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(methodElement -> {
            if (methodElement.getKind() != ElementKind.METHOD) {
                throw new GeneralException(
                        "The element {} must be a method element", methodElement.getSimpleName().toString());
            }
            Element classElement = methodElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(methodElement, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            String executorClassName = generateExecutorClass(builderContext, classElement);

            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            Map<String, String> models = classBuilder.getTransience(MODEL_COMMAND_RUN);
            if (models != null) {
                throw new GeneralException(
                        "Only one Run annotation is allowed declare in a class - {}",
                        classElement.getSimpleName().toString());
            }
            models = new HashMap<>();
            models.put("executorClassName", executorClassName);
            classBuilder.putTransience(MODEL_COMMAND_RUN, models);

            Template temp = builderContext.loadTemplate(TEMP_NEW_EXECUTOR);

            // Generate newExecutor method
            classBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .setName("newExecutor")
                    .setReturnTypeName(Type.VOID)
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(models)
                            .setTemplate(temp)));
        });
    }

    private String generateExecutorClass(IBuilderContext builderContext, Element classElement) {
        String pkgName = builderContext.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        String execClassName = classElement.getSimpleName().toString() + "_Executor_Generated";
        ClassMeta.Builder execBuilder = builderContext.newClassBuilder(pkgName, execClassName);

        // TODO: Generate all method

        return execBuilder.getQulifiedClassName();
    }
}
