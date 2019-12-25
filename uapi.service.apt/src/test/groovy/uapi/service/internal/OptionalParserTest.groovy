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
import uapi.service.IServiceLifecycle
import uapi.service.SetterMeta

import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

/**
 * Test for OptionalParser
 */
class OptionalParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new OptionalParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse on incorrect element kind'() {
        given:
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(Element) {
            getKind() >> elemKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        elemKind            | elemName
        ElementKind.PACKAGE | 'name'
        ElementKind.CLASS   | 'name'
        ElementKind.ENUM    | 'name'
    }

    def 'Test parse with no setter'() {
        given:
        def classBudr = Mock(ClassMeta.Builder) {
            findSetterBuilders() >> []
        }
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getModifiers() >> modifiers
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
            }
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        elemName    | modifiers
        'name'      | [Modifier.PUBLIC] as Set
    }

    def 'Test parse'() {
        given:
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getIsOptional() >> isOptional
            getInjectId() >> injectId
            getFieldName() >> elemName
        }]
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.addAnnotationBuilder(_) >> classBudr
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getModifiers() >> modifiers
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
            }
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        elemName    | modifiers                 | isOptional    | injectId
        'name'      | [Modifier.PUBLIC] as Set  | true          | 'ijid'
    }

    def 'Test parse on method'() {
        given:
        def injectMethod = Mock(InjectParser.InjectMethod) {
            methodName() >> elemName
            injectId() >> ijtid
            isOptional() >> true
        }
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.getTransience(InjectParser.INJECT_METHODS) >> [injectMethod]
        classBudr.findSetterBuilders() >> []
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(Element) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getModifiers() >> modifiers
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
            }
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        noExceptionThrown()
        1 * injectMethod.setOptional(true)

        where:
        elemName    | modifiers                 | isOptional    | ijtid
        'name'      | [Modifier.PUBLIC] as Set  | true          | 'ijid'
    }

    def 'Test parse on override methods'() {
        given:
        def injectMethod = Mock(InjectParser.InjectMethod) {
            methodName() >> elemName
            injectId() >> ijtid
            isOptional() >> true
            injectType() >> 'List'
        }
        def injectMethod2 = Mock(InjectParser.InjectMethod) {
            methodName() >> elemName
            injectId() >> ijtid
            isOptional() >> true
            injectType() >> 'Integer'
        }
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.getTransience(InjectParser.INJECT_METHODS) >> [injectMethod, injectMethod2]
        classBudr.findSetterBuilders() >> []
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getModifiers() >> modifiers
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
            }
            getParameters() >> [Mock(VariableElement) {
                asType() >> Mock(TypeMirror) {
                    toString() >> 'List<String>'
                }
            }]
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        noExceptionThrown()
        1 * injectMethod.setOptional(true)

        where:
        elemName    | modifiers                 | ijtid
        'name'      | [Modifier.PUBLIC] as Set  | 'ijid'
    }

    def 'Test parse on methods which has incorrect parameters'() {
        given:
        def injectMethod = Mock(InjectParser.InjectMethod) {
            methodName() >> elemName
            injectId() >> ijtid
            isOptional() >> true
            injectType() >> 'List'
        }
        def injectMethod2 = Mock(InjectParser.InjectMethod) {
            methodName() >> elemName
            injectId() >> ijtid
            isOptional() >> true
            injectType() >> 'Integer'
        }
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.getTransience(InjectParser.INJECT_METHODS) >> [injectMethod, injectMethod2]
        classBudr.findSetterBuilders() >> []
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
            isAssignable(_, IServiceLifecycle.class) >> true
        }
        def element = Mock(ExecutableElement) {
            getKind() >> ElementKind.METHOD
            getSimpleName() >> Mock(Name) {
                toString() >> elemName
            }
            getModifiers() >> modifiers
            getEnclosingElement() >> Mock(Element) {
                getModifiers() >> modifiers
            }
            getParameters() >> [Mock(VariableElement), Mock(VariableElement)]
        }
        def parser = new OptionalParser()

        when:
        parser.parse(budrCtx, [element] as Set)

        then:
        thrown(GeneralException)
        0 * injectMethod.setOptional(true)

        where:
        elemName    | modifiers                 | ijtid
        'name'      | [Modifier.PUBLIC] as Set  | 'ijid'
    }

    def 'Test helper'() {
        given:
        def classBudr = Mock(ClassMeta.Builder)
        classBudr.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getIsOptional() >> isOptional
            getInjectId() >> injectId
            getFieldName() >> elemName
        }]
        classBudr.addMethodBuilder(_) >> classBudr
        classBudr.addAnnotationBuilder(_) >> classBudr
        def budrCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport)
            findClassBuilder(_) >> classBudr
            getBuilders() >> [classBudr]
            loadTemplate(_, _) >> Mock(Template)
        }
        def parser = new OptionalParser()

        when:
        def helper = parser.getHelper()
        helper.setOptional(budrCtx, classBudr, elemName)

        then:
        helper != null

        where:
        elemName    | modifiers                 | isOptional    | injectId
        'name'      | [Modifier.PUBLIC] as Set  | true          | 'ijid'
    }
}
