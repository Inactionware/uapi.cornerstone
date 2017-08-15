/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Specification
import uapi.GeneralException
import uapi.codegen.ClassMeta
import uapi.codegen.IBuilderContext
import uapi.service.IInjectableHandlerHelper
import uapi.service.annotation.Inject
import uapi.service.annotation.Service

import javax.lang.model.element.Element

/**
 * Unit test for InjectableHandler
 */
class InjectableHandlerTest extends Specification {

    def 'Test create instance'() {
        when:
        def handler = new InjectableHandler()

        then:
        noExceptionThrown()
    }

    def 'Test get ordered annotations'() {
        when:
        def handler = new InjectableHandler()

        then:
        handler.orderedAnnotations == [Inject.class, uapi.service.annotation.Optional.class] as Class[]
    }

    def 'Test handle annotated elements'() {
        when:
        def handler = new InjectableHandler()
        def budrCtx = Mock(IBuilderContext)
        def iparser = Mock(InjectParser)
        def oparser = Mock(OptionalParser)
        handler._injectParser = iparser
        handler._optionalParser = oparser
        handler.handleAnnotatedElements(budrCtx, annoType, [Mock(Element)] as Set)

        then:
        noExceptionThrown()
//        1 * iparser.parse(_, _)
//        1 * oparser.parse(_, _)

        where:
        annoType                                | placeholder
        Inject.class                            | null
        uapi.service.annotation.Optional.class  | null
    }

    def 'Test handle annotated elements with unsupported annotation'() {
        when:
        def handler = new InjectableHandler()
        def budrCtx = Mock(IBuilderContext)
        handler.handleAnnotatedElements(budrCtx, annoType, [Mock(Element)] as Set)

        then:
        thrown(GeneralException)

        where:
        annoType       | placeholder
        Service.class  | null
        Override.class | null
    }

    def 'Test get help name'() {
        when:
        def handler = new InjectableHandler()
        def helper = handler.getHelper()

        then:
        helper.name == IInjectableHandlerHelper.name
    }

    def 'Test helper add dependency'() {
        when:
        def handler = new InjectableHandler()
        def iparser = Mock(InjectParser) {
            getHelper() >> Mock(InjectParser.InjectParserHelper)
        }
        def oparser = Mock(OptionalParser) {
            getHelper() >> Mock(OptionalParser.OptionalParserHelper)
        }
        handler._injectParser = iparser
        handler._optionalParser = oparser
        def helper = handler.getHelper()
        helper.addDependency(
                Mock(IBuilderContext),
                Mock(ClassMeta.Builder),
                'fieldName',
                'fieldType',
                'injectId',
                'injectForm',
                false, true,
                'keyType',
                true)

        then:
        noExceptionThrown()
    }
}
