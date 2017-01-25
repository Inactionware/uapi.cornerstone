/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal

import spock.lang.Specification
import uapi.Type

/**
 * Unit test for StringListValueParser
 */
class StringListValueParserTest extends Specification {

    def 'Test is support'() {
        given:
        def parser = new StringListValueParser()

        expect:
        parser.isSupport(inType, outType) == result
        parser.getName() == StringListValueParser.canonicalName

        where:
        inType                      | outType           | result
        List.canonicalName          | Type.STRING_LIST  | true
        ArrayList.canonicalName     | Type.STRING_LIST  | true
        LinkedList.canonicalName    | Type.STRING_LIST  | true
        String[].canonicalName      | Type.STRING_LIST  | false
    }

    def 'Test parse'() {
        given:
        def parser = new StringListValueParser()

        when:
        parser.parse([])
        parser.parse(new LinkedList())

        then:
        noExceptionThrown()
    }
}
