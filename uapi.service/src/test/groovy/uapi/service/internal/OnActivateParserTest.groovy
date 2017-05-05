package uapi.service.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.Type
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.codegen.LogSupport
import uapi.codegen.MethodMeta
import uapi.service.IServiceLifecycle

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * Unit test for OnActivateParser
 */
class OnActivateParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new OnActivateParser()

        then:
        noExceptionThrown()
        parser != null
    }

    def 'Test parse on error element type'() {
        given:
        def logger = Mock(LogSupport)
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
        }
        def element = Mock(ExecutableElement) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
        }
        def parser = new OnActivateParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        elementKind             | elementName
        ElementKind.CLASS       | 'test'
        ElementKind.FIELD       | 'test'
        ElementKind.CONSTRUCTOR | 'test'
    }

    def 'Test parse on element which has incorrect return type'() {
        given:
        def logger = Mock(LogSupport)
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> 'test'
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
        }
        def parser = new OnActivateParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        rtnType             | placeholder
        'String'            | null
        'java.lang.Object'  | null
        'aaa'               | null
    }

    def 'Test parse on element which has incorrect parameter count'() {
        given:
        def logger = Mock(LogSupport)
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> 'test'
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> [Mock(VariableElement)]
        }
        def parser = new OnActivateParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)
    }

    def 'Test parse'() {
        given:
        def tmpModel = [:]
        def logger = Mock(LogSupport)
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder)
        1 * classBuilder.createTransienceIfAbsent(_, _) >> tmpModel
        1 * classBuilder.findMethodBuilders(OnActivateParser.METHOD_ON_ACTIVATE_NAME) >> []
        1 * classBuilder.addImplement(IServiceLifecycle.canonicalName) >> classBuilder
        1 * classBuilder.addMethodBuilder(_)
        1 * classBuilder.getTransience(OnActivateParser.MODEL_ON_ACTIVATE) >> tmpModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
            1 * findClassBuilder(classElement) >> classBuilder
            1 * loadTemplate(_) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> 'test'
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> []
            getEnclosingElement() >> classElement
        }
        def parser = new OnActivateParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        tmpModel.size() == 1
        List<String> methods = tmpModel.get(OnActivateParser.VAR_METHODS)
        methods.size() == 1
        methods.get(0) == 'test'
    }

    def 'Test get helper'() {
        when:
        def parser = new OnActivateParser()

        then:
        parser.helper != null
    }

    def 'Test add activate method if absent'() {
        given:
        def tmpModel = [:]
        def logger = Mock(LogSupport)
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder)
        1 * classBuilder.createTransienceIfAbsent(_, _) >> tmpModel
        1 * classBuilder.findMethodBuilders(OnActivateParser.METHOD_ON_ACTIVATE_NAME) >> []
        1 * classBuilder.addImplement(IServiceLifecycle.canonicalName) >> classBuilder
        1 * classBuilder.addMethodBuilder(_)
        1 * classBuilder.getTransience(OnActivateParser.MODEL_ON_ACTIVATE) >> tmpModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
            1 * findClassBuilder(classElement) >> classBuilder
            1 * loadTemplate(_) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> 'test'
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> []
            getEnclosingElement() >> classElement
        }
        def parser = new OnActivateParser()

        when:
        parser.addOnActivateMethodIfAbsent(builderCtx, [element] as Set)

        then:
        tmpModel.size() == 1
        List<String> methods = tmpModel.get(OnActivateParser.VAR_METHODS)
        methods.size() == 0
    }

    def 'Test parse on existing element'() {
        given:
        def tmpModel = [:]
        def logger = Mock(LogSupport)
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder)
        1 * classBuilder.createTransienceIfAbsent(_, _) >> tmpModel
        1 * classBuilder.findMethodBuilders(OnActivateParser.METHOD_ON_ACTIVATE_NAME) >> [Mock(MethodMeta.Builder) {
            getReturnTypeName() >> Type.VOID
            getParameterCount() >> 0
        }]
        0 * classBuilder.addImplement(IServiceLifecycle.canonicalName) >> classBuilder
        0 * classBuilder.addMethodBuilder(_)
        0 * classBuilder.getTransience(OnActivateParser.MODEL_ON_ACTIVATE) >> tmpModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> logger
            1 * findClassBuilder(classElement) >> classBuilder
            0 * loadTemplate(_) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> 'test'
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> []
            getEnclosingElement() >> classElement
        }
        def parser = new OnActivateParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        tmpModel.size() == 1
        List<String> methods = tmpModel.get(OnActivateParser.VAR_METHODS)
        methods.size() == 1
        methods.get(0) == 'test'
    }
}
