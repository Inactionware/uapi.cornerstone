package uapi.command.internal;

import freemarker.template.Template;
import uapi.GeneralException;
import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.command.annotation.Command;
import uapi.command.annotation.MessageOutput;
import uapi.rx.Looper;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.Set;

public class MessageOutputParser {

    static final String MODEL_COMMAND_MSG_OUT_FIELD_NAME    = "messageOutputFieldName";

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
            builderContext.checkModifiers(fieldElement, MessageOutput.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            // Set up model
            ClassMeta.Builder classBuilder = builderContext.findClassBuilder(classElement);
            String msgOutFieldName = classBuilder.getTransience(MODEL_COMMAND_MSG_OUT_FIELD_NAME);
            if (msgOutFieldName != null) {
                throw new GeneralException(
                        "The MessageOutput annotation is allowed declare only once in a class - {}",
                        classElement.getSimpleName().toString());
            }
            String fieldName = fieldElement.getSimpleName().toString();
            classBuilder.putTransience(MODEL_COMMAND_MSG_OUT_FIELD_NAME, fieldName);
        });
    }
}
