package uapi.command.internal

import freemarker.template.Template
import spock.lang.Ignore
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.command.ICommandMeta
import uapi.service.annotation.handler.IServiceHandlerHelper

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements

class CommandParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new CommandParser()

        then:
        noExceptionThrown()
    }

    def 'Test parse on incorrect element'() {
        given:
        def parser = new CommandParser()
        def buildCtx = Mock(IBuilderContext)
        def element = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> clsName
            }
        }

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        thrown(GeneralException)

        where:
        elementKind         | clsName
        ElementKind.FIELD   | 'TestClass'
        ElementKind.METHOD  | 'TestClass'
        ElementKind.ENUM    | 'TestClass'
    }

    @Ignore
    def 'Test parse on element with parent command is not annotated with Command'() {
        given:
        def parser = new CommandParser()
        def buildCtx = Mock(IBuilderContext)
        def parentAnnoKey = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'parent'
            }
        }
        def parentAnnoValue = Mock(AnnotationValue) {
            getValue() >> Mock(DeclaredType) {
                asElement() >> Mock(Element) {
                    getSimpleName() >> Mock(Name) {
                        toString() >> parentClassName
                    }
                    getAnnotation(uapi.command.annotation.Command.class) >> Mock(uapi.command.annotation.Command) {
                        parent() >> void.class
                        namespace() >> ICommandMeta.DEFAULT_NAMESPACE
                        name() >> 'testCommandParent'
                        description() >> ''
                    }
                }
            }
        }
        def annoMap = new HashMap()
        annoMap.put(parentAnnoKey, parentAnnoValue)
        def annoMirror = Mock(AnnotationMirror) {
            getAnnotationType() >> Mock(DeclaredType) {
                asElement() >> Mock(Element) {
                    accept(_, _) >> Mock(TypeElement) {
                        getQualifiedName() >> Mock(Name) {
                            contentEquals(_) >> true
                        }
                    }
                }
            }
            getElementValues() >> annoMap
        }
        def element = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> clsName
            }
            getAnnotation(uapi.command.annotation.Command.class) >> Mock(uapi.command.annotation.Command) {
                parent() >> void.class
                namespace() >> ICommandMeta.DEFAULT_NAMESPACE
                name() >> 'testCommand'
                description() >> ''
            }
            getAnnotationMirrors() >> [annoMirror]
        }

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message.contains('No Command annotation was declared on class - ' + clsName)

        where:
        elementKind         | clsName   | parentClassName
        ElementKind.CLASS   | 'Test'    | ParentCommand.class.canonicalName
    }

    def 'Test parse on element with parent command which is not same namespace'() {
        given:
        def parser = new CommandParser()
        def buildCtx = Mock(IBuilderContext)
        def parentAnnoKey = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> 'parent'
            }
        }
        def parentAnnoValue = Mock(AnnotationValue) {
            getValue() >> Mock(DeclaredType) {
                asElement() >> Mock(Element) {
                    getSimpleName() >> Mock(Name) {
                        toString() >> parentClassName
                    }
                    getAnnotation(uapi.command.annotation.Command.class) >> Mock(uapi.command.annotation.Command) {
                        parent() >> void.class
                        namespace() >> 'ns'
                        name() >> 'testCommandParent'
                        description() >> ''
                    }
                }
            }
        }
        def annoMap = new HashMap()
        annoMap.put(parentAnnoKey, parentAnnoValue)
        def annoMirror = Mock(AnnotationMirror) {
            getAnnotationType() >> Mock(DeclaredType) {
                asElement() >> Mock(Element) {
                    accept(_, _) >> Mock(TypeElement) {
                        getQualifiedName() >> Mock(Name) {
                            contentEquals(_) >> true
                        }
                    }
                }
            }
            getElementValues() >> annoMap
        }
        def element = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> clsName
            }
            getAnnotation(uapi.command.annotation.Command.class) >> Mock(uapi.command.annotation.Command) {
                parent() >> ParentCommand.class
                namespace() >> ICommandMeta.DEFAULT_NAMESPACE
                name() >> 'testCommand'
                description() >> ''
            }
            getAnnotationMirrors() >> [annoMirror]
        }

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        def ex = thrown(GeneralException)
        ex.message.contains('The namespace of parent command does not equals to [] - ' + clsName)

        where:
        elementKind         | clsName   | parentClassName                   | parentNs
        ElementKind.CLASS   | 'Test'    | ParentCommand1.class.canonicalName | 'ns'
    }

    def 'Test parse no parent command'() {
        given:
        def parser = new CommandParser()
        def metaBuilder = Mock(ClassMeta.Builder) {
            1 * putTransience(CommandHandler.CMD_MODEL, _)
        }
        metaBuilder.addImplement(_) >> metaBuilder
        metaBuilder.addAnnotationBuilder(_) >> metaBuilder
        metaBuilder.addFieldBuilder(_) >> metaBuilder
        metaBuilder.addMethodBuilder(_) >> metaBuilder
        def execBuilder = Mock(ClassMeta.Builder) {
            1 * putTransience(CommandParser.FIELD_CMD_META, "_cmdMeta")
        }
        execBuilder.addImplement(_) >> execBuilder
        execBuilder.addFieldBuilder(_) >> execBuilder
        execBuilder.addMethodBuilder(_) >> execBuilder
        def buildCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getPackageOf(_) >> Mock(PackageElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> pkgName
                    }
                }
            }
            findClassBuilder(pkgName, _, false) >>> [metaBuilder, execBuilder]
            findClassBuilder(_ as Element) >> Mock(ClassMeta.Builder) {
                1 * putTransience(CommandHandler.CMD_MODEL, _)
            }
            getHelper(IServiceHandlerHelper.name) >> Mock(IServiceHandlerHelper) {
                1 * addServiceId(_, _)
            }
            loadTemplate(_, _) >> Mock(Template)
        }
        def annoMap = new HashMap()
        def annoMirror = Mock(AnnotationMirror) {
            getAnnotationType() >> Mock(DeclaredType) {
                asElement() >> Mock(Element) {
                    accept(_, _) >> Mock(TypeElement) {
                        getQualifiedName() >> Mock(Name) {
                            contentEquals(_) >> true
                        }
                    }
                }
            }
            getElementValues() >> annoMap
        }
        def element = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> clsName
            }
            getAnnotation(uapi.command.annotation.Command.class) >> Mock(uapi.command.annotation.Command) {
                parent() >> void.class
                namespace() >> ICommandMeta.DEFAULT_NAMESPACE
                name() >> 'testCommand'
                description() >> ''
            }
            getAnnotationMirrors() >> [annoMirror]
        }

        when:
        parser.parse(buildCtx, [element] as Set)

        then:
        noExceptionThrown()

        where:
        elementKind         | clsName   | parentClassName                   | pkgName
        ElementKind.CLASS   | 'Test'    | ParentCommand.class.canonicalName | 'uapi'
    }

    @uapi.command.annotation.Command(name = 'testCommand')
    class TestCommand1 { }

    @uapi.command.annotation.Command(name = 'testCommand', parent = ParentCommand.class)
    class TestCommand { }

    @uapi.command.annotation.Command(name = 'testParentCommand')
    class ParentCommand { }

    @uapi.command.annotation.Command(name = 'testParentCommand', namespace = 'ns')
    class ParentCommand1 { }
}
