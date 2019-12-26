package uapi.command.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.command.annotation.Option
import uapi.common.StringHelper
import uapi.service.annotation.Service

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

class OptionParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new OptionParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse element with incorrect type'() {
        given:
        def parser = new OptionParser()
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> elementKind
        }
        def builderCtx = Mock(IBuilderContext)

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The element {} must be a field element', elementName)

        where:
        elementName | elementKind
        'Test'      | ElementKind.CLASS
        'Test'      | ElementKind.METHOD
        'Test'      | ElementKind.ENUM
        'Test'      | ElementKind.INTERFACE
    }

    def 'Test parse element with incorrect boolean option'() {
        given:
        def parser = new OptionParser()
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
            getAnnotation(Option.class) >> Mock(Option) {
                name() >> 'optName'
                argument() >> StringHelper.EMPTY
                description() >> StringHelper.EMPTY
            }
            asType() >> Mock(TypeMirror) {
                toString() >> fieldType
            }
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Option.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        }

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The field which annotated with Option must be Boolean type - {}:{}', className, elementName)

        where:
        className   | elementName | fieldType
        'ClassName' | 'Test'      | 'String'
    }

    def 'Test parse element with incorrect string option'() {
        given:
        def parser = new OptionParser()
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
            getAnnotation(Option.class) >> Mock(Option) {
                name() >> 'optName'
                argument() >> 'arg'
                description() >> 'desc'
            }
            asType() >> Mock(TypeMirror) {
                toString() >> fieldType
            }
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Option.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        }

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString('The field which annotated with Option must be String type - {}:{}', className, elementName)

        where:
        className   | elementName | fieldType
        'ClassName' | 'Test'      | 'boolean'
    }

    def 'Test parse element success'() {
        given:
        def parser = new OptionParser()
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
            getAnnotation(Option.class) >> Mock(Option) {
                name() >> 'optName'
                argument() >> 'arg'
                description() >> 'desc'
            }
            asType() >> Mock(TypeMirror) {
                toString() >> fieldType
            }
        }
        def cmdMetaClsBuilder = Mock(ClassMeta.Builder) {
            1 * getTransience(CommandHandler.CMD_MODEL) >> new CommandModel()
        }
        cmdMetaClsBuilder.addMethodBuilder(_) >> cmdMetaClsBuilder
        def builderCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, Service.class, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Option.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                1 * addPropertyBuilder(_)
            }
            getElementUtils() >> Mock(Elements) {
                getPackageOf(classElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkgName
                    }
                }
            }
            findClassBuilder(pkgName, _, false) >> cmdMetaClsBuilder
            1 * loadTemplate(_, OptionParser.TEMP_OPTION_METAS) >> Mock(Template)
        }

        when:
        parser.parse(builderCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        className   | elementName | fieldType   | pkgName
        'ClassName' | 'Test'      | 'String'    | 'uapi'
    }
}
