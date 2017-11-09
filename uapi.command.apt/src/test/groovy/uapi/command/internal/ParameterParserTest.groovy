package uapi.command.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.codegen.PropertyMeta
import uapi.command.annotation.Parameter
import uapi.common.StringHelper

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

class ParameterParserTest extends Specification {

    def 'Test create instance'() {
        when:
        new ParameterParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse on incorrect element type'() {
        given:
        def element = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
            getKind() >> elementKind
        }
        def buildCtx = Mock(IBuilderContext)
        def parser = new ParameterParser()

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString("The element {} must be a field element", elementName)

        where:
        elementName | elementKind
        'Test'      | ElementKind.CLASS
        'Test'      | ElementKind.METHOD
        'Test'      | ElementKind.ENUM
        'Test'      | ElementKind.INTERFACE
    }

    def 'Test parse on incorrect argument type'() {
        given:
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
            getAnnotation(Parameter.class) >> Test.getDeclaredField('field').getAnnotation(Parameter.class)
            asType() >> Mock(TypeMirror) {
                toString() >> fieldType
            }
        }
        def buildCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Parameter.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        }
        def parser = new ParameterParser()

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message == StringHelper.makeString("The field which annotated with Parameter must be String type - {}:{}", className, elementName)

        where:
        elementName | className     | fieldType
        'Test'      | 'ClassName'   | 'int'
    }

    def 'Test parse success'() {
        given:
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
            getAnnotation(Parameter.class) >> Test.getDeclaredField('field').getAnnotation(Parameter.class)
            asType() >> Mock(TypeMirror) {
                toString() >> 'String'
            }
        }
        def cmdMetaClsBuilder = Mock(ClassMeta.Builder) {
            1 * getTransience(CommandHandler.CMD_MODEL) >> new CommandModel()
        }
        cmdMetaClsBuilder.addMethodBuilder(_) >> cmdMetaClsBuilder
        def buildCtx = Mock(IBuilderContext) {
            1 * checkAnnotations(classElement, uapi.command.annotation.Command.class)
            1 * checkModifiers(element, Parameter.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            findClassBuilder(classElement) >> Mock(ClassMeta.Builder) {
                1 * addPropertyBuilder(_ as PropertyMeta.Builder)
            }
            getElementUtils() >> Mock(Elements) {
                getPackageOf(classElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkgName
                    }
                }
            }
            findClassBuilder(pkgName, _, false) >> cmdMetaClsBuilder
            loadTemplate(ParameterParser.TEMP_PARAM_METAS) >> Mock(Template)
        }
        def parser = new ParameterParser()

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        elementName | className     | pkgName
        'Test'      | 'ClassName'   | 'uapi'
    }

    class Test {

        @Parameter(index = 0, name = 'param', description = 'desc')
        boolean field
    }
}
