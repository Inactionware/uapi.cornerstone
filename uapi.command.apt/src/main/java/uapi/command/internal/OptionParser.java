package uapi.command.internal;

import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.IOptionMeta;
import uapi.command.OptionType;
import uapi.command.annotation.Command;
import uapi.command.annotation.Option;
import uapi.common.StringHelper;
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

            var classElement = fieldElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(fieldElement, Option.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            var option = fieldElement.getAnnotation(Option.class);
            var optName = option.name();
            var optSName = option.shortName();
            var optArg = option.argument();
            var optDesc = option.description();
            var optField = fieldElement.getSimpleName().toString();
            var optFieldType = fieldElement.asType().toString();
            OptionType optType;
            if (StringHelper.isNullOrEmpty(optArg)) {
                if (! Type.BOOLEAN.equals(optFieldType) && ! Type.Q_BOOLEAN.equals(optFieldType)) {
                    throw new GeneralException(
                            "The field which annotated with Option must be Boolean type - {}:{}",
                            classElement.getSimpleName().toString(), optField);
                }
                optType = OptionType.Boolean;
            } else {
                if (! Type.STRING.equals(optFieldType) && ! Type.Q_STRING.equals(optFieldType)) {
                    throw new GeneralException(
                            "The field which annotated with Option must be String type - {}:{}",
                            classElement.getSimpleName().toString(), optField);
                }
                optType = OptionType.String;
            }

            // Init user command class builder
            var userCmdBuilder = builderContext.findClassBuilder(classElement);
            PropertyMeta.Builder propBuilder = PropertyMeta.builder()
                    .setFieldName(optField)
                    .setFieldType(optFieldType)
                    .setGenerateSetter(true);
            var setterName = propBuilder.setterName();
            userCmdBuilder.addPropertyBuilder(propBuilder);

            // Set up model
            var cmdMetaClassBuilder = CommandBuilderUtil.getCommandMetaBuilder(userCmdBuilder, classElement, builderContext);
            CommandModel cmdModel = cmdMetaClassBuilder.getTransience(CommandHandler.CMD_MODEL);
            var optModels = cmdModel.options;
            optModels.add(new OptionModel(optName, optSName, optArg, optDesc, optType, optField, setterName, CommandParser.FIELD_USER_CMD));
            var tmpModel = new HashMap<String, List<OptionModel>>();
            tmpModel.put("options", optModels);

            // Set up template
            var tempOptionMetas = builderContext.loadTemplate(Module.name, TEMP_OPTION_METAS);

            // Construct method
            cmdMetaClassBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .addModifier(Modifier.PUBLIC)
                    .setName("optionMetas")
                    .setReturnTypeName(Type.toArrayType(IOptionMeta.class))
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(tmpModel)
                            .setTemplate(tempOptionMetas)));
        });
    }
}
