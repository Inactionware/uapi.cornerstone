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
import uapi.service.ITagged
import uapi.service.annotation.Tag

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import java.lang.annotation.Annotation

/**
 * Unit test for TagHandler
 */
class TagHandlerTest extends Specification {

    def 'Test handleAnnotatedElements on incorrect element type'() {
        given:
        def builderCtx = Mock(IBuilderContext)
        def element = Mock(Element) {
            getKind() >> elementType
            getSimpleName() >> Mock(Name)
        }
        def elements = new HashSet<>()
        elements.add(element)
        def handler = new TagHandlerTester()

        when:
        handler.handle(builderCtx, Tag.class, elements)

        then:
        thrown(ex)

        where:
        elementType                 | ex
        ElementKind.INTERFACE       | GeneralException
        ElementKind.ANNOTATION_TYPE | GeneralException
        ElementKind.CONSTRUCTOR     | GeneralException
        ElementKind.ENUM            | GeneralException
        ElementKind.FIELD           | GeneralException
        ElementKind.METHOD          | GeneralException
        ElementKind.PACKAGE         | GeneralException
        ElementKind.TYPE_PARAMETER  | GeneralException
    }

    def 'Test handleAnnotationElements'() {
        given:
        def element = Mock(Element) {
            getKind() >> ElementKind.CLASS
            getSimpleName() >> Mock(Name)
            getAnnotation(_ as Class) >> (Tag) TagHandlerTester.class.getAnnotation(Tag.class)
        }
        def elements = new HashSet<>()
        elements.add(element)
        def classBuilder = Mock(ClassMeta.Builder) {
            getTransience(ServiceHandler.VAR_IS_PROTOTYPE, _) >> false
        }
        def builderCtx = Mock(IBuilderContext) {
            1 * checkModifiers(_ as Element, Tag.class, Modifier.PRIVATE, Modifier.FINAL)
            loadTemplate(_ as String) >> Mock(Template)
            findClassBuilder(element) >> classBuilder
        }
        def handler = new TagHandlerTester()

        when:
        handler.handle(builderCtx, Tag.class, elements)

        then:
        noExceptionThrown()
        1 * classBuilder.addImplement(ITagged.class.getCanonicalName()) >> classBuilder
    }

    @Tag('a')
    final class TagHandlerTester extends TagHandler {

        private void handle(
                final IBuilderContext builderContext,
                final Class<? extends Annotation> annotationType,
                final Set<? extends Element> elements
        ) throws GeneralException {
            super.handleAnnotatedElements(builderContext, annotationType, elements);
        }
    }
}