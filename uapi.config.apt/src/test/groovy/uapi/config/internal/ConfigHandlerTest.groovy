/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal

import freemarker.template.Template
import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.config.IConfigValueParser
import uapi.config.annotation.Config
import uapi.service.IInjectableHandlerHelper
import uapi.service.IRegistry
import uapi.service.annotation.Inject
import uapi.service.annotation.Service

import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Unit test for ConfigHandler
 */
class ConfigHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        new ConfigHandler()

        then:
        noExceptionThrown()
    }

    def 'Test get supported annotations'() {
        when:
        def handler = new ConfigHandler()

        then:
        handler.getOrderedAnnotations() == [Config.class] as Class[]
    }

    def 'Test handle annotation elements with incorrect element kind'() {
        given:
        def budrCtx = Mock(IBuilderContext)
        def elemt = Mock(Element) {
            getKind() >> elemtKind
            getSimpleName() >> Mock(Name) {
                toString() >> elemtName
            }
        }
        def handler = new ConfigHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Config.class, [elemt] as Set)

        then:
        thrown(GeneralException)

        where:
        elemtName   | elemtKind
        'Test'      | ElementKind.CLASS
        'Test'      | ElementKind.CONSTRUCTOR
        'Test'      | ElementKind.ENUM
        'Test'      | ElementKind.INTERFACE
        'Test'      | ElementKind.METHOD
    }

    def 'Test handle annotation elements'() {
        given:
        def injectRegElemt = Mock(Element) {
            getSimpleName() >> Mock(Name) {
                toString() >> injectRegElemtName
            }
        }
        def classElemt = Mock(Element) {
            getModifiers() >> [Modifier.PUBLIC]
            getAnnotation(Service.class) >> ConfigTest.class.getAnnotation(Service.class)
        }
        def annoKey = Mock(ExecutableElement) {
            getSimpleName() >> Mock(Name) {
                toString() >> elemtName
            }
        }
        def annoValue = Mock(AnnotationValue) {
            getValue() >> Mock(DeclaredType) {
                asElement() >> Mock(TypeElement) {
                    getQualifiedName() >> Mock(Name) {
                        toString() >> "ConfigParser"
                    }
                }
            }
        }
        def annoMap = new HashMap()
        annoMap.put(annoKey, annoValue)
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
        def elemt = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getSimpleName() >> Mock(Name) {
                toString() >> elemtName
            }
            asType() >> Mock(TypeMirror) {
                toString() >> elemtType
            }
            getEnclosingElement() >> classElemt
            getAnnotation(Config.class) >> {
                return ConfigTest.class.getField('_field').getAnnotation(Config.class)
            }
            getAnnotationMirrors() >> [annoMirror]
        }
        def classBudr = Mock(ClassMeta.Builder) {
            getTransience(ConfigHandler.CONFIG_INFOS) >> [Mock(ConfigHandler.ConfigInfo) {
                getPath() >> 'a.b'
                getFieldName() >> 'field'
                getFieldType() >> 'String'
                getOptional() >> false
                getParserName() >> 'ParserType'
                hasParser() >> true
            }]
            getTransience(ConfigHandler.IS_FIELD_SVC_REG_DEFINED) >> false
        }
        classBudr.addImplement(_) >> classBudr
        classBudr.addMethodBuilder(_) >> classBudr
        def budrCtx = Mock(IBuilderContext) {
            findFieldWith(classElemt, IRegistry.class, Inject.class) >> injectRegElemt
            findClassBuilder(classElemt) >> classBudr
            loadTemplate(ConfigHandler.TEMPLATE_GET_PATHS) >> Mock(Template)
            loadTemplate(ConfigHandler.TEMPLATE_IS_OPTIONAL_CONFIG) >> Mock(Template)
            loadTemplate(ConfigHandler.TEMPLATE_CONFIG) >> Mock(Template)
            getBuilders() >> [classBudr]
            getHelper(IInjectableHandlerHelper.name) >> Mock(IInjectableHandlerHelper)
        }
        def handler = new ConfigHandler()

        when:
        handler.handleAnnotatedElements(budrCtx, Config.class, [elemt] as Set)

        then:
        noExceptionThrown()

        where:
        elemtName   | elemtType | injectRegElemtName
        'Test'      | 'String'  | '_injectReg'
    }

    @Service
    class ConfigTest {

        @Config(path='a.b', parser=ConfigParser.class)
        public String _field;
    }

    class ConfigParser implements IConfigValueParser {

        @Override
        boolean isSupport(String inType, String outType) {
            return true
        }

        @Override
        String getName() {
            return "TestParser"
        }

        @Override
        def <T> T parse(Object value) {
            return null
        }
    }
}
