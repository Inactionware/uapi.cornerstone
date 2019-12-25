package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.IParameterMeta;
import uapi.command.annotation.Command;
import uapi.command.annotation.Parameter;
import uapi.rx.Looper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.*;

public class ParameterParser {

    private static final String TEMP_PARAM_METAS    = "template/parameterMetas_method.ftl";

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
            builderContext.checkAnnotations(classElement, Command.class);
            builderContext.checkModifiers(fieldElement, Parameter.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            var param = fieldElement.getAnnotation(Parameter.class);
            var paramIdx = param.index();
            var paramName = param.name();
            var paramRequired = param.required();
            var paramDesc = param.description();
            var paramFieldName = fieldElement.getSimpleName().toString();
            var paramFieldType = fieldElement.asType().toString();
            if (! Type.STRING.equals(paramFieldType) && ! Type.Q_STRING.equals(paramFieldType)) {
                throw new GeneralException(
                        "The field which annotated with Parameter must be String type - {}:{}",
                        classElement.getSimpleName().toString(), paramFieldName);
            }

            // Init user command class builder
            var userCmdBuilder = builderContext.findClassBuilder(classElement);
            PropertyMeta.Builder propBuilder = PropertyMeta.builder()
                    .setFieldName(paramFieldName)
                    .setFieldType(paramFieldType)
                    .setGenerateSetter(true);
            var setterName = propBuilder.setterName();
            userCmdBuilder.addPropertyBuilder(propBuilder);

            var cmdMetaBuilder = CommandBuilderUtil.getCommandMetaBuilder(classElement, builderContext);

            // Set up model
            CommandModel cmdModel = cmdMetaBuilder.getTransience(CommandHandler.CMD_MODEL);
            var paramModels = cmdModel.parameters;
            paramModels.add(new ParamModel(paramName, paramRequired, paramDesc, paramIdx, setterName, CommandParser.FIELD_USER_CMD, paramFieldType));
            paramModels.sort(Comparator.comparingInt(ParamModel::index));
            var tmpModel = new HashMap<String, List<ParamModel>>();
            tmpModel.put("parameters", paramModels);

            // Set up template
            var tempParamMetas = builderContext.loadTemplate(Module.name, TEMP_PARAM_METAS);

            // Construct method
            cmdMetaBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .addModifier(Modifier.PUBLIC)
                    .setName("parameterMetas")
                    .setReturnTypeName(Type.toArrayType(IParameterMeta.class))
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(tmpModel)
                            .setTemplate(tempParamMetas)));
        });
    }
}
