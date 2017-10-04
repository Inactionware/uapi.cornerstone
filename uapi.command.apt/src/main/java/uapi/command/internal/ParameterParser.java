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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ParameterParser {

    private static final String MODEL_CMD_PARAM     = "MODEL_COMMAND_PARAMETER";
    private static final String TEMP_PARAM_METAS    = "template/parameterMetas_method";

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

            // Set up model
            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            List<ParamModel> paramModels = classBuilder.getTransience(MODEL_CMD_PARAM);
            if (paramModels == null) {
                paramModels = new ArrayList<>();
                classBuilder.putTransience(MODEL_CMD_PARAM, paramModels);
            }
            ParamModel paramModel = new ParamModel();
            paramModel.index = paramIdx;
            paramModel.name = paramName;
            paramModel.required = paramRequired;
            paramModel.description = paramDesc;
            paramModels.add(paramModel);

            // Set up template
            Template tempParamMetas = builderContext.loadTemplate(TEMP_PARAM_METAS);

            // Construct method
            classBuilder.addMethodBuilder(MethodMeta.builder()
                    .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                    .addModifier(Modifier.PUBLIC)
                    .setName("parameterMetas")
                    .setReturnTypeName(Type.toArrayType(IParameterMeta.class))
                    .addCodeBuilder(CodeMeta.builder()
                            .setModel(paramModel)
                            .setTemplate(tempParamMetas)));
        });
    }
}
