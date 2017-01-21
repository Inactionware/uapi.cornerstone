/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service

import spock.lang.Specification
import uapi.GeneralException

/**
 * Unit test for SetterMeta
 */
class SetterMetaTest extends Specification {

    def 'Test build instance'() {
        when:
        def builder = SetterMeta.builder()

        then:
        noExceptionThrown()
        builder.isSetter()
    }

    def 'Test build instance with incorrect setter'() {
        when:
        def builder = SetterMeta.builder()
        builder.isSetter = false

        then:
        thrown(GeneralException)
    }

    def 'Test build instance with properties'() {
        given:
        def builder = SetterMeta.builder()

        when:
        builder.setIsSetter(true)
        builder.fieldName = fieldName
        builder.injectId = injectId
        builder.injectFrom = injectFrom
        builder.injectType = injectType
        builder.isOptional = isOptional
        builder.isSingle = isSingle
        builder.name = name
        builder.returnTypeName = rtnType
        builder.validate()
        builder.initProperties()
        def setter = builder.createInstance()

        then:
        noExceptionThrown()
        builder.isSetter()
        builder.fieldName == fieldName
        builder.injectId == injectId
        builder.injectFrom == injectFrom
        builder.injectType == injectType
        builder.isOptional == isOptional
        builder.isSingle == isSingle
        setter != null
        setter.injectType == injectType
        setter.isOptional == isOptional

        where:
        fieldName   | injectId  | injectFrom    | injectType    | isOptional    | isSingle  | name  | rtnType
        'name'      | 'abc'     | 'Local'       | 'String'      | true          | false     | 'aaa' | 'ttt'
    }
}
