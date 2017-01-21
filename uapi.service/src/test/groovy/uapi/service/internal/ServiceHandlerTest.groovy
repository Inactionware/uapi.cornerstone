/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.service.IServiceHandlerHelper
import uapi.service.annotation.Service

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * Unit test for ServiceHandler
 */
class ServiceHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        def svcHandler = new ServiceHandler()

        then:
        noExceptionThrown()
    }

    def 'Test get annotations'() {
        when:
        def svcHandle = new ServiceHandler()

        then:
        svcHandle.getOrderedAnnotations() == [Service.class] as Class[]
    }

    def 'Test helper on add new service id'() {
        when:
        def hashmap = new HashMap<String, Object>()
        def classBudr = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent('ModelGetId', _) >> hashmap
        }
        def svcHandler = new ServiceHandler()
        def helper = svcHandler.getHelper()
        helper.addServiceId(classBudr, svcId)

        then:
        helper != null
        helper.name == IServiceHandlerHelper.name
        hashmap.get('serviceIds') == [svcId] as String[]

        where:
        svcId   | placeholder
        'test'  | null
    }

    def 'Test helper on add service to existing service id'() {
        when:
        def hashmap = new HashMap<String, Object>()
        hashmap.put('serviceIds', ['abc'] as String[])
        def classBudr = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent('ModelGetId', _) >> hashmap
        }
        def svcHandler = new ServiceHandler()
        def helper = svcHandler.getHelper()
        helper.addServiceId(classBudr, svcId)

        then:
        helper != null
        helper.name == IServiceHandlerHelper.name
        hashmap.get('serviceIds') == ['abc', svcId] as String[]

        where:
        svcId   | placeholder
        'test'  | null
    }

    def 'Test handler annotation element which incorrect kind'() {
        given:
        def budrCtx = Mock(IBuilderContext)
        def classElem = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
        }
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(
                budrCtx, Service.class, [classElem] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName
        ElementKind.PARAMETER   | 'name'
        ElementKind.METHOD      | 'name'
        ElementKind.INTERFACE   | 'name'
        ElementKind.ENUM        | 'name'
        ElementKind.FIELD       | 'name'
        ElementKind.PACKAGE     | 'name'
    }

    def 'Test handle annotation element'() {
        given:
        def classElem = Mock(TypeElement) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getAnnotation(Service.class) >> TestService.getAnnotation(Service.class)
            getInterfaces() >> []
            getAnnotationMirrors() >> [Mock(AnnotationMirror) {
                getAnnotationType() >> Mock(DeclaredType) {
                    asElement() >> Mock(Element) {
                        accept(_, _) >> Mock(TypeElement) {
                            getQualifiedName() >> Mock(Name) {
                                contentEquals(_) >> true
                            }
                        }
                    }
                }
                getElementValues() >> [:]
            }]
        }

        def classBudr = Mock(ClassMeta.Builder)
        classBudr.createTransienceIfAbsent(_, _) >> new HashMap<String, Object>()
        classBudr.addAnnotationBuilder(_) >> classBudr
        classBudr.addImplement(_) >> classBudr
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.getTransience(_) >> new Object()

        def budrCtx = Mock(IBuilderContext) {
            findClassBuilder(classElem) >> classBudr
            loadTemplate(_) >> Mock(Template)
        }
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(
                budrCtx, Service.class, [classElem] as Set)

        then:
        noExceptionThrown()

        where:
        elemKind                | elemName
        ElementKind.CLASS       | 'name'
    }

    @Service
    private class TestService {}
}
