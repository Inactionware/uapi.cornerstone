package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.IParameterMeta;
import uapi.command.annotation.Command;
import uapi.command.annotation.Option;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OptionParser {

    static final String MODEL_COMMAND_OPTIONS           = "MODEL_COMMAND_OPTIONS";
    private static final String TEMP_OPTION_METAS       = "template/optionMetas_method.ftl";

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(fieldElement -> {
            if (fieldElement.getKind() != ElementKind.FIELD) {
                throw new GeneralException(
                        "The element {} must be a field element", fieldElement.getSimpleName().toString());
            }

            Element classElement = fieldElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(fieldElement, Option.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            Option option = fieldElement.getAnnotation(Option.class);
            String optName = option.name();
            char optSName = option.shortName();
            String optArg = option.argument();
            String optDesc = option.description();
            String optField = fieldElement.getSimpleName().toString();

            // Set up model
            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            List<OptionModel> optModels = classBuilder.getTransience(MODEL_COMMAND_OPTIONS);
            if (optModels == null) {
                optModels = new ArrayList<>();
                classBuilder.putTransience(MODEL_COMMAND_OPTIONS, optModels);
            }
            optModels.add(new OptionModel(optName, optSName, optArg, optDesc, optField));

            // Set up template
            Template tempOptionMetas = builderContext.loadTemplate(TEMP_OPTION_METAS);

            // Construct method
            classBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .addModifier(Modifier.PUBLIC)
                    .setName("optionMetas")
                    .setReturnTypeName(Type.toArrayType(IParameterMeta.class))
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(optModels)
                            .setTemplate(tempOptionMetas)));
        });
    }
}
