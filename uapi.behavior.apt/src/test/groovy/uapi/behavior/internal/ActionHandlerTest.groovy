/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.behavior.IExecutionContext
import uapi.behavior.annotation.Action
import uapi.behavior.annotation.ActionDo
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.codegen.MethodMeta
import uapi.rx.Looper
import uapi.service.IServiceHandlerHelper
import uapi.service.annotation.Inject
import uapi.service.annotation.Service

import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Unit test for ActionHandler
 */
class ActionHandlerTest extends Specification {

    def actionDoAnno;

    def setup() {
        actionDoAnno = Looper.on(TestClass.methods)
                .filter({method -> method.name == 'test'})
                .map({method -> method.getAnnotation(ActionDo.class)})
                .first()
        if (actionDoAnno == null) {
            throw new GeneralException("Can't find method which annotated with ActionDo annotation");
        }
    }

    def 'Test create instance'() {
        when:
        def handler = new ActionHandler()

        then:
        noExceptionThrown()
        handler.orderedAnnotations == [ Action.class ] as Class[]
    }

    def 'Test handle on incorrect annotation'() {
        when:
        def handler = new ActionHandler()
        def builderCtx = Mock(IBuilderContext)
        def elements = [ Mock(Element) ] as Set
        handler.handleAnnotatedElements(builderCtx, annoType, elements)

        then:
        thrown(GeneralException)

        where:
        annoType       | placeholder
        Service.class  | null
        Inject.class   | null
        Override.class | null
    }

    def 'Test handle on incorrect element'() {
        when:
        def handler = new ActionHandler()
        def buildCtx = Mock(IBuilderContext)
        def element = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        handler.handleAnnotatedElements(buildCtx, Action.class, [ element ] as Set)

        then:
        thrown(GeneralException)

        where:
        elementKind             | className
        ElementKind.FIELD       | 'Test'
        ElementKind.METHOD      | 'Test'
        ElementKind.ENUM        | 'Test'
        ElementKind.INTERFACE   | 'Test'
    }

    def 'Test handle element which has no ActionDo defined'() {
        when:
        def handler = new ActionHandler()
        def element = Mock(Element) {
            getKind() >> ElementKind.CLASS
            getSimpleName() >> Mock(Name) {
                toString() >> 'TestClass'
            }
            asType() >> Mock(TypeMirror) {
                toString() >> 'TestClass'
            }
            getAnnotation(Action.class) >> TestClass.getAnnotation(Action.class)
            getEnclosedElements() >> []
        }
        def builderCtx = Mock(IBuilderContext) {
            checkAnnotations(element, Service.class as Class[]) >> true
        }
        handler.handleAnnotatedElements(builderCtx, Action.class, [ element ] as Set)

        then:
        thrown(GeneralException)
    }

    def 'Test handle element which has more ActionDo defined'() {
        when:

        def handler = new ActionHandler()
        def element = Mock(Element) {
            getKind() >> ElementKind.CLASS
            getSimpleName() >> Mock(Name) {
                toString() >> 'TestClass'
            }
            asType() >> Mock(TypeMirror) {
                toString() >> 'TestClass'
            }
            getAnnotation(Action.class) >> TestClass.getAnnotation(Action.class)
            getEnclosedElements() >> [Mock(Element) {
                getKind() >> ElementKind.METHOD
                getAnnotation(ActionDo.class) >> actionDoAnno
            }, Mock(Element) {
                getKind() >> ElementKind.METHOD
                getAnnotation(ActionDo.class) >> actionDoAnno
            }]
        }
        def builderCtx = Mock(IBuilderContext) {
            checkAnnotations(element, Service.class as Class[]) >> true
        }
        handler.handleAnnotatedElements(builderCtx, Action.class, [ element ] as Set)

        then:
        thrown(GeneralException)
    }

    def 'Test handle element'() {
        when:
        def handler = new ActionHandler()
        def element = Mock(Element) {
            getKind() >> ElementKind.CLASS
            getSimpleName() >> Mock(Name) {
                toString() >> 'TestClass'
            }
            asType() >> Mock(TypeMirror) {
                toString() >> 'TestClass'
            }
            getAnnotation(Action.class) >> TestClass.getAnnotation(Action.class)
            getEnclosedElements() >> [Mock(ExecutableElement) {
                getKind() >> ElementKind.METHOD
                getAnnotation(ActionDo.class) >> actionDoAnno
                getSimpleName() >> Mock(Name) {
                    toString() >> 'MethodName'
                }
                getParameters() >> [Mock(VariableElement) {
                    asType() >> Mock(TypeMirror) {
                        toString() >> Object.canonicalName
                    }
                }, Mock(VariableElement) {
                    asType() >> Mock(TypeMirror) {
                        toString() >> IExecutionContext.canonicalName
                    }
                }]
                getReturnType() >> Mock(TypeMirror) {
                    toString() >> String.canonicalName
                }
            }]
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
        def clsBuilder = Mock(ClassMeta.Builder)
        def builderCtx = Mock(IBuilderContext) {
            checkAnnotations(element, Service.class as Class[]) >> true
            loadTemplate(_) >> Mock(Template)
            findClassBuilder(element) >> clsBuilder
            1 * getHelper(IServiceHandlerHelper.name) >> Mock(IServiceHandlerHelper) {
                1 * addServiceId(_, _)
            }
        }
        1 * clsBuilder.addImplement(_ as String) >> clsBuilder
        5 * clsBuilder.addMethodBuilder(_ as MethodMeta.Builder) >> clsBuilder
        handler.handleAnnotatedElements(builderCtx, Action.class, [ element ] as Set)

        then:
        noExceptionThrown()
    }

    @Action()
    class TestClass {

        @ActionDo
        public static void test() {}
    }
}
