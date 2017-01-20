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
import spock.lang.Ignore
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.codegen.LogSupport
import uapi.codegen.MethodMeta
import uapi.service.IInitial
import uapi.service.IInitialHandlerHelper
import uapi.service.annotation.Init

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * Test for InitialHandler
 */
class InitialHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        def handler = new InitialHandler()

        then:
        noExceptionThrown()
    }

    def 'Test get ordered annotations'() {
        when:
        def handler = new InitialHandler()

        then:
        handler.orderedAnnotations == [Init.class] as Class[]
    }

    def 'Test helper add init method for it have init method'() {
        given:
        def initMethodBuilder = Mock(MethodMeta.Builder) {
            getReturnTypeName() >> 'void'
            getParameterCount() >> 0
        }
        def hashmap = existingMethods
        def budrCtx = Mock(IBuilderContext)
        def classBudr = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent('ModelInit', _) >> hashmap
            findMethodBuilders('init') >> [initMethodBuilder]
        }

        when:
        def handler = new InitialHandler()
        def helper = handler.getHelper()
        helper.addInitMethod(budrCtx, classBudr, target, initMethodName)

        then:
        helper != null
        helper.name == IInitialHandlerHelper.name

        where:
        existingMethods                     | target    | initMethodName
        [:]                                 | 'a'       | 'b'
        ['methods': ['ab'] as String[]]     | 'a'       | 'b'
    }

    def 'Test helper add init method'() {
        given:
        def hashmap = existingMethods
        def budrCtx = Mock(IBuilderContext) {
            loadTemplate(_) >> Mock(Template)
        }
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.createTransienceIfAbsent('ModelInit', _) >> hashmap
        classBudr.findMethodBuilders('init') >> []
        classBudr.addImplement(_) >> classBudr
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.getTransience(_) >> new Object()

        when:
        def handler = new InitialHandler()
        def helper = handler.getHelper()
        helper.addInitMethod(budrCtx, classBudr, target, initMethodName)

        then:
        helper != null
        helper.name == IInitialHandlerHelper.name

        where:
        existingMethods                     | target    | initMethodName
        [:]                                 | 'a'       | 'b'
        ['methods': ['ab'] as String[]]     | 'a'       | 'b'
    }

    def 'Test handle annotated element with incorrect element kind'() {
        given:
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
        }
        def elemt = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
        }
        def handler = new InitialHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Init.class, [elemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName
        ElementKind.PACKAGE     | 'name'
        ElementKind.FIELD       | 'name'
    }

    def 'Test handle annotated element with incorrect method parameters'() {
        given:
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
        }
        def elemt = Mock(ExecutableElement) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
                getModifiers() >> modifiers
            }
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
            getThrownTypes() >> [Mock(TypeMirror) {
                toString() >> exType
            }]
            getParameters() >> [Mock(VariableElement) {
                getKind() >> ElementKind.PARAMETER
                getSimpleName() >> Mock(Name) {
                    toString() >> paramName
                }
                asType() >> Mock(TypeMirror) {
                    toString() >> paramType
                }
            }]
            getModifiers() >> modifiers
        }
        def handler = new InitialHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Init.class, [elemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName  | modifiers                 | rtnType   | exType                | paramName | paramType
        ElementKind.METHOD      | 'name'    | [Modifier.PUBLIC] as Set  | 'String'  | 'RuntimeException'    | 'pname'   | 'Int'
    }

    def 'Test handle annotated element on class has defined an init method'() {
        given:
        def hashmap = [:]
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.createTransienceIfAbsent('ModelInit', _) >> hashmap
        classBudr.findMethodBuilders('init') >> [Mock(MethodMeta.Builder)]
        classBudr.addImplement(_) >> classBudr
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.getTransience(_) >> new Object()
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            loadTemplate(_) >> Mock(Template)
            findClassBuilder(_) >> classBudr
        }
        def elemt = Mock(ExecutableElement) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
                getModifiers() >> modifiers
            }
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
            getThrownTypes() >> [Mock(TypeMirror) {
                toString() >> exType
            }]
            getParameters() >> []
            getModifiers() >> modifiers
        }
        def handler = new InitialHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Init.class, [elemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind                | elemName  | modifiers                 | rtnType   | exType
        ElementKind.METHOD      | 'name'    | [Modifier.PUBLIC] as Set  | 'String'  | 'RuntimeException'
    }

    def 'Test handle annotated element'() {
        given:
        def hashmap = [:]
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.createTransienceIfAbsent('ModelInit', _) >> hashmap
        classBudr.findMethodBuilders('init') >> []
        classBudr.addImplement(_) >> classBudr
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.getTransience(_) >> new Object()
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            loadTemplate(_) >> Mock(Template)
            findClassBuilder(_) >> classBudr
        }
        def elemt = Mock(ExecutableElement) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
                getModifiers() >> modifiers
            }
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
            getThrownTypes() >> [Mock(TypeMirror) {
                toString() >> exType
            }]
            getParameters() >> []
            getModifiers() >> modifiers
        }
        def handler = new InitialHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Init.class, [elemt] as Set)

        then:
        noExceptionThrown()

        where:
        elemKind                | elemName  | modifiers                 | rtnType   | exType
        ElementKind.METHOD      | 'name'    | [Modifier.PUBLIC] as Set  | 'String'  | 'RuntimeException'
    }
}