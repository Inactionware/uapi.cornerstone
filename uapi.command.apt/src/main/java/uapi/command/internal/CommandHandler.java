package uapi.command.internal;

import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.codegen.AnnotationsHandler;
import uapi.codegen.IAnnotationsHandler;
import uapi.codegen.IBuilderContext;
import uapi.command.annotation.*;
import uapi.common.ArgumentChecker;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

@AutoService(IAnnotationsHandler.class)
public class CommandHandler extends AnnotationsHandler {

    public static final String CMD_MODEL    = "COMMAND_MODEL";

    private CommandParser       _cmdParser;
    private ParameterParser     _paramParser;
    private OptionParser        _optParser;
    private MessageOutputParser _outputParser;
    private RunParser           _runParser;

    public CommandHandler() {
        this._cmdParser = new CommandParser();
        this._paramParser = new ParameterParser();
        this._optParser = new OptionParser();
        this._outputParser = new MessageOutputParser();
        this._runParser = new RunParser();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return new Class[] {
                uapi.command.annotation.Command.class,
                Parameter.class,
                Option.class,
                MessageOutput.class,
                Run.class};
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        ArgumentChecker.required(annotationType, "annotationType");

        if (annotationType.equals(uapi.command.annotation.Command.class)) {
            this._cmdParser.parse(builderContext, elements);
        } else if (annotationType.equals(Parameter.class)) {
            this._paramParser.parse(builderContext, elements);
        } else if (annotationType.equals(Option.class)) {
            this._optParser.parse(builderContext, elements);
        } else if (annotationType.equals(MessageOutput.class)) {
            this._outputParser.parse(builderContext, elements);
        } else if(annotationType.equals(Run.class)) {
            this._runParser.parse(builderContext, elements);
        } else {
            throw new GeneralException("Unsupported annotation - {}", annotationType.getName());
        }
    }
}
