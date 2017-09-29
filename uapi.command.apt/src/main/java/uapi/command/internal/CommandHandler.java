package uapi.command.internal;

import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.codegen.AnnotationsHandler;
import uapi.codegen.IAnnotationsHandler;
import uapi.codegen.IBuilderContext;
import uapi.command.annotation.*;
import uapi.command.annotation.Command;
import uapi.common.ArgumentChecker;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

@AutoService(IAnnotationsHandler.class)
public class CommandHandler extends AnnotationsHandler {

    private final CommandParser     _cmdParser;
    private final ParameterParser   _paramParser;
    private final OptionParser      _optParser;
    private final OutParser         _outParser;
    private final RunParser         _runParser;

    public CommandHandler() {
        this._cmdParser = new CommandParser();
        this._paramParser = new ParameterParser();
        this._optParser = new OptionParser();
        this._outParser = new OutParser();
        this._runParser = new RunParser();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return new Class[] { uapi.command.annotation.Command.class, Parameter.class, Option.class, Out.class, Run.class};
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        ArgumentChecker.required(annotationType, "annotationType");

        if (annotationType.equals(Command.class)) {
            this._cmdParser.parse(builderContext, elements);
        } else if (annotationType.equals(Parameter.class)) {
            this._paramParser.parse(builderContext, elements);
        } else if (annotationType.equals(Option.class)) {
            this._optParser.parse(builderContext, elements);
        } else if (annotationType.equals(Out.class)) {
            this._outParser.parse(builderContext, elements);
        } else if(annotationType.equals(Run.class)) {
            this._runParser.parse(builderContext, elements);
        } else {
            throw new GeneralException("Unsupported annotation - {}", annotationType.getName());
        }
    }
}
