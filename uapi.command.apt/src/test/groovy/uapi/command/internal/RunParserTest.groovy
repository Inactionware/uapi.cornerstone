package uapi.command.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.command.annotation.Run
import uapi.common.StringHelper
import uapi.service.annotation.Service

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

class RunParserTest extends Specification {

    def 'Test create instance'() {
        when:
        new RunParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse on element with incorrect type'() {
        given:
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> elementKind
        }
        def builderCtx = Mock(IBuilderContext)
        def parser = new RunParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The element {} must be a method element', elementName)

        where:
        elementName | elementKind
        'Test'      | ElementKind.CLASS
        'Test'      | ElementKind.FIELD
        'Test'      | ElementKind.ENUM
        'Test'      | ElementKind.INTERFACE
    }

    def 'Test parse on element with incorrect method parameter'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.METHOD
            getEnclosingElement() >> classElement
            getParameters() >> [Mock(VariableElement)]
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        }
        def parser = new RunParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The method annotated with Run must be has no input parameter - {}', elementName)

        where:
        elementName | className
        'Test'      | 'ClassName'
    }

    def 'Test parse on element with incorrect method return type'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.METHOD
            getEnclosingElement() >> classElement
            getParameters() >> []
            getReturnType() >> Mock(TypeMirror) {
                toString() >> rtnType
            }
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        }
        def parser = new RunParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The method annotated with Run must return a boolean type - {}', elementName)

        where:
        elementName | className     | rtnType
        'Test'      | 'ClassName'   | 'String'
    }

    def 'Test parse on element which define more run method'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.METHOD
            getEnclosingElement() >> classElement
            getParameters() >> []
            getReturnType() >> Mock(TypeMirror) {
                toString() >> 'boolean'
            }
        }
        def cmdMetaClsBuilder = Mock(ClassMeta.Builder) {
            1 * getTransience(CommandHandler.CMD_MODEL) >> new CommandModel()
        }
        cmdMetaClsBuilder.addMethodBuilder(_) >> cmdMetaClsBuilder
        def cmdExecClsBuilder = Mock(ClassMeta.Builder) {
            getTransience(RunParser.MODEL_COMMAND_EXECUTOR) >> new HashMap()
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            getElementUtils() >> Mock(Elements) {
                getPackageOf(classElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkgName
                    }
                }
            }
            findClassBuilder(pkgName, _ as String, false) >>> [cmdMetaClsBuilder, cmdExecClsBuilder]
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                getPackageName() >> pkgName
            }
        }
        def parser = new RunParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('Only one Run annotation is allowed declare in a class - {}', className)

        where:
        elementName | className     | pkgName
        'Test'      | 'ClassName'   | 'uapi'
    }

    def 'Test parse success'() {
        given:
        def classElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> className
            }
        }
        def element = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> ElementKind.METHOD
            getEnclosingElement() >> classElement
            getParameters() >> []
            getReturnType() >> Mock(TypeMirror) {
                toString() >> 'boolean'
            }
        }
        def cmdMetaClsBuilder = Mock(ClassMeta.Builder) {
            1 * getTransience(CommandHandler.CMD_MODEL) >> new CommandModel()
        }
        cmdMetaClsBuilder.addMethodBuilder(_) >> cmdMetaClsBuilder
        def cmdExecClsBuilder = Mock(ClassMeta.Builder) {
            getTransience(RunParser.MODEL_COMMAND_EXECUTOR) >> null
            1 * getTransience(MessageOutputParser.MODEL_COMMAND_MSG_OUT_FIELD_NAME) >> msgField
            1 * putTransience(RunParser.MODEL_COMMAND_EXECUTOR, _)
        }
        cmdExecClsBuilder.addImplement(_) >> cmdExecClsBuilder
        cmdExecClsBuilder.addFieldBuilder(_) >> cmdExecClsBuilder
        cmdExecClsBuilder.addMethodBuilder(_) >> cmdExecClsBuilder
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Run.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            getElementUtils() >> Mock(Elements) {
                getPackageOf(classElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkgName
                    }
                }
            }
            findClassBuilder(pkgName, _ as String, false) >>> [cmdMetaClsBuilder, cmdExecClsBuilder]
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                getPackageName() >> pkgName
            }
            5 * loadTemplate(_, _ as String) >> Mock(Template)
        }
        def parser = new RunParser()

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        elementName | className     | pkgName   | msgField
        'Test'      | 'ClassName'   | 'uapi'    | 'output'
    }
}
