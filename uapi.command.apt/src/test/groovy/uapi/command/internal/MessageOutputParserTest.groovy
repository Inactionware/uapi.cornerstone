package uapi.command.internal

import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.command.annotation.MessageOutput
import uapi.common.StringHelper
import uapi.service.annotation.Service

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name

class MessageOutputParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new MessageOutputParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse on incorrect element'() {
        given:
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> elementKind
        }
        def parser = new MessageOutputParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString("The element {} must be a field element", elementName)

        where:
        elementKind         | elementName
        ElementKind.CLASS   | 'Test'
        ElementKind.METHOD  | 'Test2'
        ElementKind.ENUM    | 'Test3'
    }

    def 'Test parse on define multiple message output filed'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> classElement
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, MessageOutput.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                1 * getTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME) >> msgOutField
            }
        }
        def parser = new MessageOutputParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString("The MessageOutput annotation is allowed declare only once in a class - {}", className)

        where:
        msgOutField         | elementName   | className
        'output'            | 'Test'        | 'ClassName'
    }

    def 'Test parse success'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> classElement
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, MessageOutput.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                1 * getTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME)
                1 * putTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME, elementName)
            }
        }
        def parser = new MessageOutputParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        msgOutField         | elementName   | className
        'output'            | 'Test'        | 'ClassName'
    }
}
