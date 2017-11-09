package uapi.command.internal

import spock.lang.Specification
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext

import javax.lang.model.element.Element
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.util.Elements

class CommandBuilderUtilTest extends Specification {

    def 'Test get command meta builder'() {
        given:
        def mockCmdMetaBuilder = Mock(ClassMeta.Builder)
        def clsElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> cmdClassName
            }
        }
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getPackageOf(clsElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> cmdClassPkg
                    }
                }
            }
            findClassBuilder(cmdClassPkg, _, false) >> null
            newClassBuilder(cmdClassPkg, _) >> mockCmdMetaBuilder
        }

        when:
        def cmdMetaBuilder = CommandBuilderUtil.getCommandMetaBuilder(clsElement, builderCtx)

        then:
        noExceptionThrown()
        cmdMetaBuilder != null
        cmdMetaBuilder == mockCmdMetaBuilder

        where:
        cmdClassPkg     | cmdClassName
        'com'           | 'Command'
    }

    def 'Test get command executor builder'() {
        given:
        def mockCmdExecBuiler = Mock(ClassMeta.Builder)
        def clsElement = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> cmdClassName
            }
        }
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getPackageOf(clsElement) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> cmdClassPkg
                    }
                }
            }
            findClassBuilder(cmdClassPkg, _, false) >> null
            newClassBuilder(cmdClassPkg, _) >> mockCmdExecBuiler
        }

        when:
        def cmdExecBuilder = CommandBuilderUtil.getCommandExecutorBuilder(clsElement, builderCtx)

        then:
        noExceptionThrown()
        cmdExecBuilder != null
        cmdExecBuilder == mockCmdExecBuiler

        where:
        cmdClassPkg     | cmdClassName
        'org'           | 'MyTest'
    }
}
