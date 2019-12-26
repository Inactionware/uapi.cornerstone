package uapi.service.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.Type
import uapi.codegen.*
import uapi.common.StringHelper
import uapi.service.IServiceLifecycle
import uapi.service.annotation.OnInject

import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

/**
 * Unit tests for OnInjectParser
 */
class OnInjectParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new OnInjectParser()

        then:
        noExceptionThrown()
        parser != null
        parser.helper != null
    }

    def 'Test parser on incorrect element type'() {
        given:
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                1 * info(_ as String)
            }
        }
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'methodElement'
            }
            getKind() >> elementKind
        }
        def parser = new OnInjectParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        elementKind             | placeholder
        ElementKind.CLASS       | null
        ElementKind.FIELD       | null
        ElementKind.ENUM        | null
        ElementKind.CONSTRUCTOR | null
    }

    def 'Test parser on incorrect method return type'() {
        given:
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                1 * info(_ as String)
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'method'
            }
            getKind() >> ElementKind.METHOD
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
        }
        def parser = new OnInjectParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        rtnType             | placeholder
        Type.Q_ARRAY_LIST   | null
        Type.INTEGER        | null
        Type.Q_STRING       | null
    }

    def 'Test parser on incorrect method parameter count'() {
        given:
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                1 * info(_ as String)
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'method'
            }
            getKind() >> ElementKind.METHOD
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> params
        }
        def parser = new OnInjectParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        params                                          | placeholder
        []                                              | null
        [Mock(VariableElement), Mock(VariableElement)]  | null
    }

    def 'Test parser'() {
        given:
        def tempInjectModel = [:]
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent(OnInjectParser.MODEL_ON_INJECT, _) >> tempInjectModel
        }
        1 * classBuilder.findMethodBuilders(_) >> []
        1 * classBuilder.addImplement(IServiceLifecycle.class.canonicalName) >> classBuilder
        1 * classBuilder.addMethodBuilder(_) >> classBuilder
        1 * classBuilder.getTransience(OnInjectParser.MODEL_ON_INJECT) >> tempInjectModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                1 * info(_ as String)
            }
            findClassBuilder(classElement) >> classBuilder
            1 * loadTemplate(_, _) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'method'
            }
            getEnclosingElement() >> classElement
            getKind() >> ElementKind.METHOD
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> [Mock(VariableElement) {
                asType() >> Mock(TypeMirror) {
                    toString() >> paramType
                }
            }]
            1 * getAnnotation(_) >> Mock(OnInject) {
                value() >> StringHelper.EMPTY
            }
        }
        def parser = new OnInjectParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        noExceptionThrown()
        tempInjectModel.size() == 1
        List methods = tempInjectModel.get(OnInjectParser.VAR_METHODS)
        methods.size() == 1
        Map method = methods.get(0)
        method[OnInjectParser.VAR_METHOD_NAME] == 'method'
        method[OnInjectParser.VAR_SVC_ID] == paramType
        method[OnInjectParser.VAR_SVC_TYPE] == paramType

        where:
        paramType       | placeholder
        'String'        | null
    }

    def 'Test parser on existing builder'() {
        given:
        def tempInjectModel = [:]
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent(OnInjectParser.MODEL_ON_INJECT, _) >> tempInjectModel
        }
        1 * classBuilder.findMethodBuilders(_) >> [Mock(MethodMeta.Builder) {
            getReturnTypeName() >> Type.VOID
            getParameterCount() >> 2
            findParameterBuilder(OnInjectParser.PARAM_SVC_ID) >> Mock(ParameterMeta.Builder) {
                1 * getType() >> Type.Q_STRING
            }
            findParameterBuilder(OnInjectParser.PARAM_SVC) >> Mock(ParameterMeta.Builder) {
                1 * getType() >> Type.Q_OBJECT
            }
        }]
        0 * classBuilder.addImplement(IServiceLifecycle.class.canonicalName) >> classBuilder
        0 * classBuilder.addMethodBuilder(_) >> classBuilder
        0 * classBuilder.getTransience(OnInjectParser.MODEL_ON_INJECT) >> tempInjectModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                1 * info(_ as String)
            }
            findClassBuilder(classElement) >> classBuilder
            0 * loadTemplate(_, _) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'method'
            }
            getEnclosingElement() >> classElement
            getKind() >> ElementKind.METHOD
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> [Mock(VariableElement) {
                asType() >> Mock(TypeMirror) {
                    toString() >> paramType
                }
            }]
            1 * getAnnotation(_) >> Mock(OnInject) {
                value() >> StringHelper.EMPTY
            }
        }
        def parser = new OnInjectParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        noExceptionThrown()
        tempInjectModel.size() == 1
        List methods = tempInjectModel.get(OnInjectParser.VAR_METHODS)
        methods.size() == 1
        Map method = methods.get(0)
        method[OnInjectParser.VAR_METHOD_NAME] == 'method'
        method[OnInjectParser.VAR_SVC_ID] == paramType
        method[OnInjectParser.VAR_SVC_TYPE] == paramType

        where:
        paramType       | placeholder
        'String'        | null
    }


    def 'Test add inject method if absent'() {
        given:
        def tempInjectModel = [:]
        def classElement = Mock(Element)
        def classBuilder = Mock(ClassMeta.Builder) {
            createTransienceIfAbsent(OnInjectParser.MODEL_ON_INJECT, _) >> tempInjectModel
        }
        1 * classBuilder.findMethodBuilders(_) >> [Mock(MethodMeta.Builder) {
            getReturnTypeName() >> Type.VOID
            getParameterCount() >> 2
            findParameterBuilder(OnInjectParser.PARAM_SVC_ID) >> Mock(ParameterMeta.Builder) {
                1 * getType() >> Type.Q_STRING
            }
            findParameterBuilder(OnInjectParser.PARAM_SVC) >> Mock(ParameterMeta.Builder) {
                1 * getType() >> Type.Q_OBJECT
            }
        }]
        0 * classBuilder.addImplement(IServiceLifecycle.class.canonicalName) >> classBuilder
        0 * classBuilder.addMethodBuilder(_) >> classBuilder
        0 * classBuilder.getTransience(OnInjectParser.MODEL_ON_INJECT) >> tempInjectModel
        def builderCtx = Mock(IBuilderContext) {
            getLogger() >> Mock(LogSupport) {
                0 * info(_ as String)
            }
            findClassBuilder(classElement) >> classBuilder
            0 * loadTemplate(_, _) >> Mock(Template)
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'method'
            }
            getEnclosingElement() >> classElement
            getKind() >> ElementKind.METHOD
            getReturnType() >> Mock(TypeMirror) {
                toString() >> Type.VOID
            }
            getParameters() >> [Mock(VariableElement) {
                asType() >> Mock(TypeMirror) {
                    toString() >> paramType
                }
            }]
            0 * getAnnotation(_) >> Mock(OnInject) {
                value() >> StringHelper.EMPTY
            }
        }
        def parser = new OnInjectParser()

        when:
        parser.addInjectMethodIfAbsent(builderCtx, [element] as Set)

        then:
        noExceptionThrown()
        tempInjectModel.size() == 1
        List methods = tempInjectModel.get(OnInjectParser.VAR_METHODS)
        methods.size() == 0

        where:
        paramType       | placeholder
        'String'        | null
    }
}
