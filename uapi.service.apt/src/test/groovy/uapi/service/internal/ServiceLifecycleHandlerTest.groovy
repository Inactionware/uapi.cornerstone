package uapi.service.internal

import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.service.annotation.helper.IServiceLifecycleHandlerHelper
import uapi.service.annotation.OnActivate
import uapi.service.annotation.OnDeactivate
import uapi.service.annotation.OnInject
import uapi.service.annotation.Service

import javax.lang.model.element.Element

/**
 * Unit tests for ServiceLifecycleHandler
 */
class ServiceLifecycleHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        def handler = new ServiceLifecycleHandler()

        then:
        noExceptionThrown()
        handler.helper != null
        handler.getOrderedAnnotations() == [OnActivate.class, OnInject.class, OnDeactivate.class]
    }

    def 'Test handle annotation element on incorrect annotation'() {
        given:
        def builderContext = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        def handler = new ServiceLifecycleHandler()

        when:
        handler.handleAnnotatedElements(builderContext, Service.class, elements)

        then:
        thrown(GeneralException)
    }

    def 'Test handle annotation element for OnActivate annotation'() {
        given:
        def builderContext = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        def onActivateParser = Mock(OnActivateParser) {
            1 * parse(builderContext, elements)
            1 * addOnActivateMethodIfAbsent(builderContext, elements)
        }
        def onInjectParser = Mock(OnInjectParser) {
            0 * parse(builderContext, elements)
            1 * addInjectMethodIfAbsent(builderContext, elements)
        }
        def onDeactivateParser = Mock(OnDeactivateParser) {
            0 * parse(builderContext, elements)
            1 * addOnDeactivateMethodIfAbsent(builderContext, elements)
        }
        def handler = new ServiceLifecycleHandler()
        handler._onActivateParser = onActivateParser
        handler._onInjectParser = onInjectParser
        handler._onDeactivateParser = onDeactivateParser

        when:
        handler.handleAnnotatedElements(builderContext, OnActivate.class, elements)

        then:
        noExceptionThrown()
    }

    def 'Test handle annotation element for OnInject annotation'() {
        given:
        def builderContext = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        def onActivateParser = Mock(OnActivateParser) {
            0 * parse(builderContext, elements)
            1 * addOnActivateMethodIfAbsent(builderContext, elements)
        }
        def onInjectParser = Mock(OnInjectParser) {
            1 * parse(builderContext, elements)
            1 * addInjectMethodIfAbsent(builderContext, elements)
        }
        def onDeactivateParser = Mock(OnDeactivateParser) {
            0 * parse(builderContext, elements)
            1 * addOnDeactivateMethodIfAbsent(builderContext, elements)
        }
        def handler = new ServiceLifecycleHandler()
        handler._onActivateParser = onActivateParser
        handler._onInjectParser = onInjectParser
        handler._onDeactivateParser = onDeactivateParser

        when:
        handler.handleAnnotatedElements(builderContext, OnInject.class, elements)

        then:
        noExceptionThrown()
    }

    def 'Test handle annotation element for OnDeactivate annotation'() {
        given:
        def builderContext = Mock(IBuilderContext)
        def element = Mock(Element)
        def elements = [element] as Set
        def onActivateParser = Mock(OnActivateParser) {
            0 * parse(builderContext, elements)
            1 * addOnActivateMethodIfAbsent(builderContext, elements)
        }
        def onInjectParser = Mock(OnInjectParser) {
            0 * parse(builderContext, elements)
            1 * addInjectMethodIfAbsent(builderContext, elements)
        }
        def onDeactivateParser = Mock(OnDeactivateParser) {
            1 * parse(builderContext, elements)
            1 * addOnDeactivateMethodIfAbsent(builderContext, elements)
        }
        def handler = new ServiceLifecycleHandler()
        handler._onActivateParser = onActivateParser
        handler._onInjectParser = onInjectParser
        handler._onDeactivateParser = onDeactivateParser

        when:
        handler.handleAnnotatedElements(builderContext, OnDeactivate.class, elements)

        then:
        noExceptionThrown()
    }

    def 'Test handler helper'() {
        given:
        def classBuilder = Mock(ClassMeta.Builder)
        def builderContext = Mock(IBuilderContext)
        def onActivateParser = Mock(OnActivateParser) {
            1 * getHelper() >> Mock(OnActivateParser.OnActivateHelper) {
                1 * addActivateMethod(builderContext, classBuilder, _ as String[])
            }
        }
        def onInjectParser = Mock(OnInjectParser) {
            1 * getHelper() >> Mock(OnInjectParser.OnInjectHelper) {
                1 * addInjectMethod(builderContext, classBuilder, _ as String, _ as String, _ as String)
            }
        }
        def onDeactivateParser = Mock(OnDeactivateParser) {
            1 * getHelper() >> Mock(OnDeactivateParser.OnDeactivateHelper) {
                1 * addDeactivateMethod(builderContext, classBuilder, _ as String[])
            }
        }
        def handler = new ServiceLifecycleHandler()
        handler._onActivateParser = onActivateParser
        handler._onInjectParser = onInjectParser
        handler._onDeactivateParser = onDeactivateParser

        when:
        ((IServiceLifecycleHandlerHelper) handler.getHelper()).addActivateMethod(builderContext, classBuilder, 'method')
        ((IServiceLifecycleHandlerHelper) handler.getHelper()).addInjectMethod(builderContext, classBuilder, 'method', 'sid', 'stype')
        ((IServiceLifecycleHandlerHelper) handler.getHelper()).addDeactivateMethod(builderContext, classBuilder, 'method')

        then:
        noExceptionThrown()
    }
}
