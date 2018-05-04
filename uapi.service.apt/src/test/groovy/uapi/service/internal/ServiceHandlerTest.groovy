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
import uapi.codegen.LogSupport
import uapi.service.IServiceHandlerHelper
import uapi.service.ServiceType
import uapi.service.annotation.Attribute
import uapi.service.annotation.Service

import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import java.lang.annotation.ElementType

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
        svcHandle.getOrderedAnnotations() == [Service.class, Attribute.class] as Class[]
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
            getLogger() >> Mock(LogSupport)
            getElementUtils() >> Mock(Elements) {
                getPackageOf(classElem) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkg
                    }
                }
            }
        }
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(
                budrCtx, Service.class, [classElem] as Set)

        then:
        noExceptionThrown()

        where:
        elemKind                | elemName  | pkg
        ElementKind.CLASS       | 'name'    | 'com.ee'
    }

    def 'Test handle Attribute annotation on incorrect element type'() {
        given:
        def fieldElemt = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
        }
        def budrCtx = Mock(IBuilderContext)
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(budrCtx, Attribute.class, [fieldElemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName
        ElementKind.CLASS       | 'name'
        ElementKind.INTERFACE   | 'name'
    }

    def 'Test handle Attribute annotation on non-prototype service'() {
        given:
        def attrMap = new HashMap<String, Object>()
        def clsElemt = Mock(Element) {
            getAnnotation(Service.class) >> TestService.getAnnotation(Service.class)
            getSimpleName() >> Mock(Name) {
                toString() >> 'className'
            }
        }
        def fieldElemt = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getEnclosingElement() >> clsElemt
        }
        def budrCtx = Mock(IBuilderContext)
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(budrCtx, Attribute.class, [fieldElemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName
        ElementKind.FIELD       | 'name'
    }

    def 'Test handle prototype service'() {
        given:
        def attrMap = new HashMap<String, Object>()
        def clsElemt = Mock(TypeElement) {
            getAnnotation(Service.class) >> PrototypeService.getAnnotation(Service.class)
            getSimpleName() >> Mock(Name) {
                toString() >> 'className'
            }
            getKind() >> ElementKind.CLASS
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
        def instClsBudr = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent(_, _) >> attrMap
        }
        1 * instClsBudr.addImplement(_) >> instClsBudr
        1 * instClsBudr.addFieldBuilder(_) >> instClsBudr
        4 * instClsBudr.addMethodBuilder(_) >> instClsBudr
        2 * instClsBudr.putTransience(_, _)
        def protoClsBudr = Mock(ClassMeta.Builder)
        1 * protoClsBudr.addAnnotationBuilder(_) >> protoClsBudr
        1 * protoClsBudr.addImplement(_) >> protoClsBudr
        4 * protoClsBudr.addMethodBuilder(_) >> protoClsBudr
        def budrCtx = Mock(IBuilderContext) {
            4 * loadTemplate(_) >> Mock(Template)
            findClassBuilder(clsElemt) >> instClsBudr
            1 * newClassBuilder(_, _) >> protoClsBudr
            getLogger() >> Mock(LogSupport)
            getElementUtils() >> Mock(Elements) {
                getPackageOf(clsElemt) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> 'pkgname'
                    }
                }
            }
        }
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(budrCtx, Service.class, [clsElemt] as Set)

        then:
        noExceptionThrown()

//        where:
//        elemKind                | elemName
//        ElementKind.FIELD       | 'name'
    }

    def 'Test handle prototype service attribute'() {
        given:
        def attrMap = new HashMap<String, Object>()
        def clsElemt = Mock(Element) {
            getAnnotation(Service.class) >> PrototypeService.getAnnotation(Service.class)
            getSimpleName() >> Mock(Name) {
                toString() >> 'className'
            }
        }
        def fieldElemt = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getAnnotation(Attribute.class) >> PrototypeService.getField('name').getAnnotation(Attribute.class)
            asType() >> Mock(TypeMirror) {
                toString() >> 'java.lang.String'
            }
            getEnclosingElement() >> clsElemt
        }
        def budrCtx = Mock(IBuilderContext) {
            findClassBuilder(clsElemt) >> Mock(ClassMeta.Builder) {
                createTransienceIfAbsent(_, _) >> attrMap
            }
        }
        def svcHandler = new ServiceHandler()

        when:
        svcHandler.handleAnnotatedElements(budrCtx, Attribute.class, [fieldElemt] as Set)

        then:
        noExceptionThrown()
        attrMap.size() == 1

        where:
        elemKind                | elemName
        ElementKind.FIELD       | 'name'
    }

    @Service
    static class TestService {

        @Attribute('attrName')
        public String name
    }

    @Service(type=ServiceType.Prototype)
    class PrototypeService {

        @Attribute('attrName')
        public static name
    }
}
