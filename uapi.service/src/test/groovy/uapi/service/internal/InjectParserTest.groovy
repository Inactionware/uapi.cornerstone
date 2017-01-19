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
import uapi.service.SetterMeta
import uapi.service.annotation.Inject

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Unit test for InjectParser
 */
class InjectParserTest extends Specification {

    def 'Test get helper'() {
        given:
        def parser = new InjectParser()

        expect:
        parser.getHelper() != null
    }

    def 'Test parse on incorrect element type'() {
        given:
        def builderCtx = Mock(IBuilderContext)
        def fieldElement = Mock(Element) {
            getKind() >> elementKind
            getSimpleName() >> Mock(Name) {
                toString() >> elementName
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        thrown(ex)

        where:
        elementKind             | elementName   | ex
        ElementKind.CLASS       | 'FieldName'   | GeneralException
        ElementKind.CONSTRUCTOR | 'FieldName'   | GeneralException
        ElementKind.ENUM        | 'FieldName'   | GeneralException
        ElementKind.INTERFACE   | 'FieldName'   | GeneralException
        ElementKind.METHOD      | 'FieldName'   | GeneralException
        ElementKind.PARAMETER   | 'FieldName'   | GeneralException
    }

    def 'Test parse on collection field element with incorrect generic type'() {
        given:
        def collectionTypeElement = Mock(TypeElement)
        def mapTypeElement = Mock(TypeElement)
        def collectionType = Mock(DeclaredType)
        def mapType = Mock(DeclaredType)
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getTypeElement(Collection.canonicalName) >> collectionTypeElement
                getTypeElement(Map.canonicalName) >> mapTypeElement
            }
            getTypeUtils() >> Mock(Types) {
                getwildcardType(_, _) >> Mock(WildcardType)
                getDeclaredType(collectionTypeElement, _) >> collectionType
                getDeclaredType(mapTypeElement, _, _) >> mapType
                isAssignable(_, collectionType) >> isCollection
                isAssignable(_, mapType) >> isMap
            }
        }
        def fieldElement = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> Mock(Element) {
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getSimpleName() >> Mock(Name) {
                toString() >> fieldName
            }
            asType() >> Mock(DeclaredType) {
                toString() >> fieldType
                getTypeArguments() >> [Mock(TypeMirror), Mock(TypeMirror)]
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        thrown(GeneralException)

        where:
        fieldName   | fieldType | isCollection  | isMap
        'test'      | 'String'  | true          | false
    }

    def 'Test parse on collection field element'() {
        given:
        def collectionTypeElement = Mock(TypeElement)
        def mapTypeElement = Mock(TypeElement)
        def collectionType = Mock(DeclaredType)
        def mapType = Mock(DeclaredType)
        def classBuilder = Mock(ClassMeta.Builder)
        classBuilder.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getInjectId() >> 'test'
            getInjectFrom() >> 'Local'
            getIsSingle() >> true
            getName() >> 'name'
            getInjectType() >> 'String'
        }]
        classBuilder.addImplement(_) >> classBuilder
        classBuilder.addMethodBuilder(_) >> classBuilder
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getTypeElement(Collection.canonicalName) >> collectionTypeElement
                getTypeElement(Map.canonicalName) >> mapTypeElement
            }
            getTypeUtils() >> Mock(Types) {
                getwildcardType(_, _) >> Mock(WildcardType)
                getDeclaredType(collectionTypeElement, _) >> collectionType
                getDeclaredType(mapTypeElement, _, _) >> mapType
                isAssignable(_, collectionType) >> isCollection
                isAssignable(_, mapType) >> isMap
            }
            findClassBuilder(_) >> classBuilder
            getBuilders() >> [classBuilder]
            loadTemplate(_) >> Mock(Template)
        }
        def fieldElement = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> Mock(Element) {
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getSimpleName() >> Mock(Name) {
                toString() >> fieldName
            }
            asType() >> Mock(DeclaredType) {
                toString() >> fieldType
                getTypeArguments() >> [Mock(TypeMirror) {
                    toString() >> genericType
                }]
            }
            getAnnotation(_) >> {
                return Test.class.getField('t').getAnnotation(Inject.class)
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        noExceptionThrown()

        where:
        fieldName   | fieldType | isCollection  | isMap | genericType
        'test'      | 'String'  | true          | false | 'Integer'
    }

    def 'Test parse on map field element with incorrect generic type'() {
        given:
        def collectionTypeElement = Mock(TypeElement)
        def mapTypeElement = Mock(TypeElement)
        def collectionType = Mock(DeclaredType)
        def mapType = Mock(DeclaredType)
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getTypeElement(Collection.canonicalName) >> collectionTypeElement
                getTypeElement(Map.canonicalName) >> mapTypeElement
            }
            getTypeUtils() >> Mock(Types) {
                getwildcardType(_, _) >> Mock(WildcardType)
                getDeclaredType(collectionTypeElement, _) >> collectionType
                getDeclaredType(mapTypeElement, _, _) >> mapType
                isAssignable(_, collectionType) >> {
                    return isCollection
                }
                isAssignable(_, mapType) >> {
                    return isMap
                }
            }
        }
        def fieldElement = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> Mock(Element) {
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getSimpleName() >> Mock(Name) {
                toString() >> fieldName
            }
            asType() >> Mock(DeclaredType) {
                toString() >> fieldType
                getTypeArguments() >> [Mock(TypeMirror)]
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        thrown(GeneralException)

        where:
        fieldName   | fieldType | isCollection  | isMap
        'test'      | 'String'  | false         | true
    }

    def 'Test parse on map field element does not implement IIdentifiable'() {
        given:
        def collectionTypeElement = Mock(TypeElement)
        def mapTypeElement = Mock(TypeElement)
        def collectionType = Mock(DeclaredType)
        def mapType = Mock(DeclaredType)
        def classBuilder = Mock(ClassMeta.Builder)
        classBuilder.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getInjectId() >> 'test'
            getInjectFrom() >> 'Local'
            getIsSingle() >> true
            getName() >> 'name'
            getInjectType() >> 'String'
        }]
        classBuilder.addImplement(_) >> classBuilder
        classBuilder.addMethodBuilder(_) >> classBuilder
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getTypeElement(Collection.canonicalName) >> collectionTypeElement
                getTypeElement(Map.canonicalName) >> mapTypeElement
            }
            getTypeUtils() >> Mock(Types) {
                getwildcardType(_, _) >> Mock(WildcardType)
                getDeclaredType(collectionTypeElement, _) >> collectionType
                getDeclaredType(mapTypeElement, _, _) >> mapType
                isAssignable(_, collectionType) >> isCollection
                isAssignable(_, mapType) >> isMap
            }
            findClassBuilder(_) >> classBuilder
            getBuilders() >> [classBuilder]
            loadTemplate(_) >> Mock(Template)
        }
        def fieldElement = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> Mock(Element) {
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getSimpleName() >> Mock(Name) {
                toString() >> fieldName
            }
            asType() >> Mock(DeclaredType) {
                toString() >> fieldType
                getTypeArguments() >> [Mock(TypeMirror) {
                    toString() >> keyType
                }, Mock(TypeMirror) {
                    toString() >> genericType
                }]
            }
            getAnnotation(_) >> {
                return Test.class.getField('t').getAnnotation(Inject.class)
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        thrown(GeneralException)

        where:
        fieldName   | fieldType | isCollection  | isMap | genericType   | keyType
        'test'      | 'String'  | false         | true  | 'Integer'     | 'String'
    }

    def 'Test parse on map field element'() {
        given:
        def collectionTypeElement = Mock(TypeElement)
        def mapTypeElement = Mock(TypeElement)
        def collectionType = Mock(DeclaredType)
        def mapType = Mock(DeclaredType)
        def classBuilder = Mock(ClassMeta.Builder)
        def keyType = Mock(DeclaredType) {
            toString() >> kt
        }
        def valueType = Mock(DeclaredType) {
            toString() >> vt
        }
        classBuilder.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getInjectId() >> 'test'
            getInjectFrom() >> 'Local'
            getIsSingle() >> true
            getName() >> 'name'
            getInjectType() >> 'String'
        }]
        classBuilder.addImplement(_) >> classBuilder
        classBuilder.addMethodBuilder(_) >> classBuilder
        def builderCtx = Mock(IBuilderContext) {
            getElementUtils() >> Mock(Elements) {
                getTypeElement(Collection.canonicalName) >> collectionTypeElement
                getTypeElement(Map.canonicalName) >> mapTypeElement
            }
            getTypeUtils() >> Mock(Types) {
                getwildcardType(_, _) >> Mock(WildcardType)
                getDeclaredType(collectionTypeElement, _) >> collectionType
                getDeclaredType(mapTypeElement, _, _) >> mapType
                isAssignable(_, collectionType) >> isCollection
                isAssignable(_, mapType) >> isMap
                isAssignable(valueType, _) >> true
            }
            findClassBuilder(_) >> classBuilder
            getBuilders() >> [classBuilder]
            loadTemplate(_) >> Mock(Template)
        }
        def fieldElement = Mock(Element) {
            getKind() >> ElementKind.FIELD
            getEnclosingElement() >> Mock(Element) {
                getSimpleName() >> Mock(Name) {
                    toString() >> 'className'
                }
            }
            getSimpleName() >> Mock(Name) {
                toString() >> fieldName
            }
            asType() >> Mock(DeclaredType) {
                toString() >> fieldType
                getTypeArguments() >> [keyType, valueType]
            }
            getAnnotation(_) >> {
                return Test.class.getField('t').getAnnotation(Inject.class)
            }
        }
        def parser = new InjectParser()

        when:
        parser.parse(builderCtx, [fieldElement] as Set)

        then:
        noExceptionThrown()

        where:
        fieldName   | fieldType | isCollection  | isMap | vt        | kt
        'test'      | 'String'  | false         | true  | 'Integer' | 'String'
    }

    def 'Test add dependency'() {
        given:
        def classBuilder = Mock(ClassMeta.Builder)
        classBuilder.findSetterBuilders() >> [Mock(SetterMeta.Builder) {
            getInjectId() >> 'test'
            getInjectFrom() >> 'Local'
            getIsSingle() >> true
            getName() >> 'name'
            getInjectType() >> 'String'
        }]
        classBuilder.addImplement(_) >> classBuilder
        classBuilder.addMethodBuilder(_) >> classBuilder
        def builderCtx = Mock(IBuilderContext) {
            loadTemplate() >> Mock(Template)
            findClassBuilder(_) >> classBuilder
            getBuilders() >> [classBuilder]
            loadTemplate(_) >> Mock(Template)
        }
        def parser = new InjectParser()
        def helper = parser.getHelper()

        when:
        helper.addDependency(builderCtx, classBuilder,
                fieldName, fieldType,
                injectId, injectFrom,
                isCollection, isMap,mapKeyType)

        then:
        noExceptionThrown()

        where:
        fieldName   | fieldType | injectId  | injectFrom    | isCollection  | isMap | mapKeyType
        'name'      | 'String'  | 'id'      | 'Local'       | true          | false | 'Int'
    }
}

class Test {

    @Inject('iid')
    public String t;
}
