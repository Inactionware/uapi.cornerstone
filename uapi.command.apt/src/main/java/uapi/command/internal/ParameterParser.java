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

            Element classElement = fieldElement.getEnclosingElement();
            builderContext.checkAnnotations(classElement, Command.class);
            builderContext.checkModifiers(fieldElement, Parameter.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            Parameter param = fieldElement.getAnnotation(Parameter.class);
            int paramIdx = param.index();
            String paramName = param.name();
            boolean paramRequired = param.required();
            String paramDesc = param.description();
            String paramFieldName = fieldElement.getSimpleName().toString();
            String paramFieldType = fieldElement.asType().toString();
            if (! Type.STRING.equals(paramFieldType) && ! Type.Q_STRING.equals(paramFieldType)) {
                throw new GeneralException(
                        "The field which annotated with Parameter must be String type - {}:{}",
                        classElement.getSimpleName().toString(), paramFieldName);
            }

            // Init user command class builder
            ClassMeta.Builder userCmdBuilder = builderContext.findClassBuilder(classElement);
            PropertyMeta.Builder propBuilder = PropertyMeta.builder()
                    .setFieldName(paramFieldName)
                    .setFieldType(paramFieldType)
                    .setGenerateSetter(true);
            String setterName = propBuilder.setterName();
            userCmdBuilder.addPropertyBuilder(propBuilder);

            ClassMeta.Builder cmdMetaBuilder = CommandBuilderUtil.getCommandMetaBuilder(classElement, builderContext);

            // Set up model
            CommandModel cmdModel = cmdMetaBuilder.getTransience(CommandHandler.CMD_MODEL);
            List<ParamModel> paramModels = cmdModel.parameters;
            paramModels.add(new ParamModel(paramName, paramRequired, paramDesc, paramIdx, setterName, CommandParser.FIELD_USER_CMD, paramFieldType));
            paramModels.sort(Comparator.comparingInt(ParamModel::index));
            Map<String, List<ParamModel>> tmpModel = new HashMap<>();
            tmpModel.put("parameters", paramModels);

            // Set up template
            Template tempParamMetas = builderContext.loadTemplate(TEMP_PARAM_METAS);

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
