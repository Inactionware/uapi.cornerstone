package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.command.IParameterMeta;
import uapi.command.annotation.Command;
import uapi.command.annotation.Parameter;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.*;

public class ParameterParser {

    static final String MODEL_CMD_PARAM             = "MODEL_COMMAND_PARAMETER";
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
            builderContext.checkAnnotations(classElement, Service.class, Command.class);
            builderContext.checkModifiers(fieldElement, Parameter.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            Parameter param = fieldElement.getAnnotation(Parameter.class);
            int paramIdx = param.index();
            String paramName = param.name();
            boolean paramRequired = param.required();
            String paramDesc = param.description();
            String paramField = fieldElement.getSimpleName().toString();

            // Set up model
            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            List<ParamModel> paramModels = classBuilder.getTransience(MODEL_CMD_PARAM);
            if (paramModels == null) {
                paramModels = new ArrayList<>();
                classBuilder.putTransience(MODEL_CMD_PARAM, paramModels);
            }
            paramModels.add(new ParamModel(paramName, paramRequired, paramDesc, paramIdx, paramField));
            paramModels.sort(Comparator.comparingInt(ParamModel::index));
            Map<String, List<ParamModel>> tmpModel = new HashMap<>();
            tmpModel.put("parameters", paramModels);

            // Set up template
            Template tempParamMetas = builderContext.loadTemplate(TEMP_PARAM_METAS);

            // Construct method
            classBuilder.addMethodBuilder(MethodMeta.builder()
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
