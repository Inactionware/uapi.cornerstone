package uapi.command.internal

import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.IBuilderContext
import uapi.command.annotation.MessageOutput
import uapi.command.annotation.Option
import uapi.command.annotation.Parameter
import uapi.command.annotation.Run
import uapi.service.annotation.Service

import javax.lang.model.element.Element

class CommandHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        def cmdHandler = new CommandHandler()

        then:
        noExceptionThrown()
        cmdHandler.getOrderedAnnotations() == [ uapi.command.annotation.Command.class,
                                               Parameter.class,
                                               Option.class,
                                               MessageOutput.class,
                                               Run.class ]
    }

    def 'Test handle command annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, uapi.command.annotation.Command.class, elements)

        then:
        noExceptionThrown()
        1 * cmdHandler._cmdParser.parse(builderCtx, elements)
        0 * cmdHandler._paramParser.parse(builderCtx, elements)
        0 * cmdHandler._optParser.parse(builderCtx, elements)
        0 * cmdHandler._outputParser.parse(builderCtx, elements)
        0 * cmdHandler._runParser.parse(builderCtx, elements)
    }

    def 'Test handle parameter annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, Parameter.class, elements)

        then:
        noExceptionThrown()
        0 * cmdHandler._cmdParser.parse(builderCtx, elements)
        1 * cmdHandler._paramParser.parse(builderCtx, elements)
        0 * cmdHandler._optParser.parse(builderCtx, elements)
        0 * cmdHandler._outputParser.parse(builderCtx, elements)
        0 * cmdHandler._runParser.parse(builderCtx, elements)
    }

    def 'Test handle option annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, Option.class, elements)

        then:
        noExceptionThrown()
        0 * cmdHandler._cmdParser.parse(builderCtx, elements)
        0 * cmdHandler._paramParser.parse(builderCtx, elements)
        1 * cmdHandler._optParser.parse(builderCtx, elements)
        0 * cmdHandler._outputParser.parse(builderCtx, elements)
        0 * cmdHandler._runParser.parse(builderCtx, elements)
    }

    def 'Test handle output annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, MessageOutput.class, elements)

        then:
        noExceptionThrown()
        0 * cmdHandler._cmdParser.parse(builderCtx, elements)
        0 * cmdHandler._paramParser.parse(builderCtx, elements)
        0 * cmdHandler._optParser.parse(builderCtx, elements)
        1 * cmdHandler._outputParser.parse(builderCtx, elements)
        0 * cmdHandler._runParser.parse(builderCtx, elements)
    }

    def 'Test handle run annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, Run.class, elements)

        then:
        noExceptionThrown()
        0 * cmdHandler._cmdParser.parse(builderCtx, elements)
        0 * cmdHandler._paramParser.parse(builderCtx, elements)
        0 * cmdHandler._optParser.parse(builderCtx, elements)
        0 * cmdHandler._outputParser.parse(builderCtx, elements)
        1 * cmdHandler._runParser.parse(builderCtx, elements)
    }

    def 'Test handle un-recognized annotation element'() {
        given:
        def cmdHandler = new CommandHandler()
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        cmdHandler._cmdParser = Mock(CommandParser)
        cmdHandler._paramParser = Mock(ParameterParser)
        cmdHandler._optParser = Mock(OptionParser)
        cmdHandler._outputParser = Mock(MessageOutputParser)
        cmdHandler._runParser = Mock(RunParser)

        when:
        cmdHandler.handleAnnotatedElements(builderCtx, Service.class, elements)

        then:
        thrown(GeneralException)
        0 * cmdHandler._cmdParser.parse(builderCtx, elements)
        0 * cmdHandler._paramParser.parse(builderCtx, elements)
        0 * cmdHandler._optParser.parse(builderCtx, elements)
        0 * cmdHandler._outputParser.parse(builderCtx, elements)
        0 * cmdHandler._runParser.parse(builderCtx, elements)
    }
}
