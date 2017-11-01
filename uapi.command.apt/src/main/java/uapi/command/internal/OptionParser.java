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
import java.util.*;

public class OptionParser {

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
            String optFieldType = fieldElement.asType().toString();
            if (! Type.STRING.equals(optFieldType) || ! Type.Q_STRING.equals(optFieldType)) {
                throw new GeneralException(
                        "The field which annotated with Parameter must be String type - {}:{}",
                        classElement.getSimpleName().toString(), optFieldType);
            }

            // Init user command class builder
            ClassMeta.Builder userCmdBuilder = builderContext.findClassBuilder(classElement);
            PropertyMeta.Builder propBuilder = PropertyMeta.builder()
                    .setFieldName(optField)
                    .setFieldType(optFieldType)
                    .setGenerateSetter(true);
            String setterName = propBuilder.setterName();
            userCmdBuilder.addPropertyBuilder(propBuilder);

            // Set up model
            ClassMeta.Builder cmdMetaClassBuilder = CommandBuilderUtil.getCommandMetaBuilder(classElement, builderContext);
            CommandModel cmdModel = cmdMetaClassBuilder.getTransience(CommandHandler.CMD_MODEL);
            List<OptionModel> optModels = cmdModel.options;
            optModels.add(new OptionModel(optName, optSName, optArg, optDesc, optField, setterName, CommandParser.FIELD_USER_CMD));
            Map<String, List<OptionModel>> tmpModel = new HashMap<>();
            tmpModel.put("parameters", optModels);

            // Set up template
            Template tempOptionMetas = builderContext.loadTemplate(TEMP_OPTION_METAS);

            // Construct method
            cmdMetaClassBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .addModifier(Modifier.PUBLIC)
                    .setName("optionMetas")
                    .setReturnTypeName(Type.toArrayType(IParameterMeta.class))
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(tmpModel)
                            .setTemplate(tempOptionMetas)));
        });
    }
}
